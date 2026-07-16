import React, { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { schedulerApi } from '../api/schedulerApi';
import './SchedulerPage.css';

// 비로그인 유저의 관심 게임 필터는 로컬에 보관, 로그인 유저는 서버(DB)에 저장한다.
const FILTER_STORAGE_KEY = 'scheduler_game_filter';

const CATEGORY_LABELS = { UPDATE: '업데이트', EVENT: '이벤트', MAINTENANCE: '점검' };

function addMonths(date, n) {
  const d = new Date(date);
  d.setMonth(d.getMonth() + n);
  return d;
}

function formatDate(d) {
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
}

function monthTicks(from, to) {
  const ticks = [];
  const d = new Date(from.getFullYear(), from.getMonth(), 1);
  while (d <= to) {
    if (d >= from) ticks.push(new Date(d));
    d.setMonth(d.getMonth() + 1);
  }
  return ticks;
}

function loadLocalFilter() {
  try {
    const raw = localStorage.getItem(FILTER_STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function SchedulerPage() {
  const { isAuthenticated } = useAuth();
  const [games, setGames] = useState(null);
  const [schedules, setSchedules] = useState(null);
  const [selectedCodes, setSelectedCodes] = useState([]);
  const [rangeFrom, setRangeFrom] = useState(() => addMonths(new Date(), -1));
  const [rangeTo, setRangeTo] = useState(() => addMonths(new Date(), 3));
  const [error, setError] = useState(null);

  useEffect(() => {
    schedulerApi
      .fetchGames()
      .then(setGames)
      .catch(() => setError('게임 목록을 불러오지 못했습니다.'));
  }, []);

  useEffect(() => {
    let cancelled = false;
    setSchedules(null);
    schedulerApi
      .fetchSchedules({ from: rangeFrom.toISOString(), to: rangeTo.toISOString() })
      .then((data) => {
        if (!cancelled) setSchedules(data);
      })
      .catch(() => {
        if (!cancelled) setError('일정을 불러오지 못했습니다.');
      });
    return () => {
      cancelled = true;
    };
  }, [rangeFrom, rangeTo]);

  // 필터 초기값: 로그인 시 서버 저장값, 비로그인 시 localStorage
  useEffect(() => {
    if (isAuthenticated) {
      schedulerApi
        .fetchMyGamePreferences()
        .then((prefs) => setSelectedCodes(prefs.map((p) => p.gameCode)))
        .catch(() => setSelectedCodes(loadLocalFilter()));
    } else {
      setSelectedCodes(loadLocalFilter());
    }
  }, [isAuthenticated]);

  const toggleGame = async (gameCode) => {
    const next = selectedCodes.includes(gameCode)
      ? selectedCodes.filter((c) => c !== gameCode)
      : [...selectedCodes, gameCode];
    setSelectedCodes(next);
    if (isAuthenticated) {
      try {
        await schedulerApi.updateMyGamePreferences(next);
      } catch {
        setError('필터 저장에 실패했습니다.');
      }
    } else {
      localStorage.setItem(FILTER_STORAGE_KEY, JSON.stringify(next));
    }
  };

  const shiftRange = (months) => {
    setRangeFrom((prev) => addMonths(prev, months));
    setRangeTo((prev) => addMonths(prev, months));
  };

  const resetRange = () => {
    setRangeFrom(addMonths(new Date(), -1));
    setRangeTo(addMonths(new Date(), 3));
  };

  const visibleSchedules = useMemo(() => {
    if (schedules == null) return null;
    const filtered =
      selectedCodes.length === 0
        ? schedules
        : schedules.filter((s) => selectedCodes.includes(s.gameCode));
    return [...filtered].sort(
      (a, b) => (a.gameTitle ?? '').localeCompare(b.gameTitle ?? '') || new Date(a.startAt) - new Date(b.startAt),
    );
  }, [schedules, selectedCodes]);

  const fromMs = rangeFrom.getTime();
  const toMs = rangeTo.getTime();
  const position = (date) => Math.max(0, Math.min(1, (new Date(date).getTime() - fromMs) / (toMs - fromMs)));
  const ticks = monthTicks(rangeFrom, rangeTo);
  const now = new Date();
  const todayVisible = now >= rangeFrom && now <= rangeTo;

  return (
    <div className="scheduler-page">
      <h1>게임 업데이트 스케줄러</h1>

      <div className="scheduler-toolbar">
        <button type="button" onClick={() => shiftRange(-1)}>◀ 이전 달</button>
        <span className="scheduler-range-label">
          {formatDate(rangeFrom)} ~ {formatDate(rangeTo)}
        </span>
        <button type="button" onClick={() => shiftRange(1)}>다음 달 ▶</button>
        <button type="button" onClick={resetRange}>오늘</button>
      </div>

      {games != null && games.length > 0 && (
        <div className="scheduler-filter">
          <span className="scheduler-filter-hint">
            관심 게임을 선택하면 해당 게임의 일정만 표시됩니다 (선택 없음 = 전체 표시
            {isAuthenticated ? ', 선택은 계정에 저장됩니다' : ', 로그인하면 기기 간 선택이 유지됩니다'})
          </span>
          {games.map((game) => (
            <label key={game.gameCode}>
              <input
                type="checkbox"
                checked={selectedCodes.includes(game.gameCode)}
                onChange={() => toggleGame(game.gameCode)}
              />
              {game.title}
            </label>
          ))}
        </div>
      )}

      <div className="scheduler-legend">
        {Object.entries(CATEGORY_LABELS).map(([key, label]) => (
          <span key={key} className={`cat-${key.toLowerCase()}`}>{label}</span>
        ))}
      </div>

      {error && <p className="scheduler-error">{error}</p>}

      <div className="gantt">
        <div className="gantt-inner">
          <div className="gantt-header">
            <div className="gantt-label" />
            <div className="gantt-track gantt-months">
              {ticks.map((tick) => (
                <div
                  key={tick.getTime()}
                  className="gantt-month-tick"
                  style={{ left: `${position(tick) * 100}%` }}
                >
                  {tick.getFullYear()}.{String(tick.getMonth() + 1).padStart(2, '0')}
                </div>
              ))}
            </div>
          </div>

          {visibleSchedules == null ? (
            <div className="gantt-empty">일정을 불러오는 중...</div>
          ) : visibleSchedules.length === 0 ? (
            <div className="gantt-empty">표시할 일정이 없습니다.</div>
          ) : (
            visibleSchedules.map((event) => {
              const start = position(event.startAt);
              // endAt이 없는 일정(상시/종료 미정)은 조회 범위 끝까지 이어지는 것으로 표시
              const end = event.endAt ? position(event.endAt) : 1;
              return (
                <div key={event.id} className="gantt-row">
                  <div className="gantt-label" title={event.description ?? ''}>
                    <span className="gantt-event-title">{event.title}</span>
                    <span className="gantt-game-title">
                      {event.gameTitle ?? event.gameCode} · {CATEGORY_LABELS[event.category] ?? event.category}
                    </span>
                  </div>
                  <div className="gantt-track">
                    {ticks.map((tick) => (
                      <div
                        key={tick.getTime()}
                        className="gantt-grid-line"
                        style={{ left: `${position(tick) * 100}%` }}
                      />
                    ))}
                    {todayVisible && <div className="gantt-today" style={{ left: `${position(now) * 100}%` }} />}
                    <div
                      className={`gantt-bar cat-${String(event.category).toLowerCase()}`}
                      style={{ left: `${start * 100}%`, width: `${Math.max((end - start) * 100, 1)}%` }}
                      title={`${event.title} (${formatDate(new Date(event.startAt))}${event.endAt ? ` ~ ${formatDate(new Date(event.endAt))}` : ' ~'})`}
                    />
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}

export default SchedulerPage;
