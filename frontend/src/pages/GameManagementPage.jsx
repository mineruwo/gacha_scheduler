import React, { useEffect, useState } from 'react';
import { adminApi, schedulerApi } from '../api/schedulerApi';
import './GameManagementPage.css';

const CATEGORY_LABELS = { UPDATE: '업데이트', EVENT: '이벤트', MAINTENANCE: '점검' };

const GAME_FLAGS = [
  ['hasGacha', '가챠 있음'],
  ['hasPass', '패스 있음'],
  ['canRecord', '기록 가능'],
  ['isService', '서비스 중'],
  ['canManageSchedule', '일정 관리 대상'],
  ['canTrackCurrency', '재화 추적 가능'],
];

const emptyGameForm = {
  title: '',
  gameCode: '',
  hasGacha: false,
  hasPass: false,
  canRecord: false,
  isService: true,
  comment: '',
  canManageSchedule: true,
  canTrackCurrency: false,
};

const emptyScheduleForm = {
  gameCode: '',
  title: '',
  category: 'EVENT',
  startAt: '',
  endAt: '',
  description: '',
};

// OffsetDateTime(ISO) ↔ <input type="datetime-local"> 값 변환
function toLocalInput(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function toIso(local) {
  return local ? new Date(local).toISOString() : null;
}

function addMonths(date, n) {
  const d = new Date(date);
  d.setMonth(d.getMonth() + n);
  return d;
}

function GameManagementPage() {
  const [games, setGames] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [gameForm, setGameForm] = useState(emptyGameForm);
  const [editingGameId, setEditingGameId] = useState(null);
  const [scheduleForm, setScheduleForm] = useState(emptyScheduleForm);
  const [editingScheduleId, setEditingScheduleId] = useState(null);
  const [error, setError] = useState(null);

  const loadGames = () =>
    adminApi
      .fetchGames()
      .then(setGames)
      .catch(() => setError('게임 목록을 불러오지 못했습니다. 관리자 권한을 확인하세요.'));

  const loadSchedules = () =>
    schedulerApi
      .fetchSchedules({
        from: addMonths(new Date(), -1).toISOString(),
        to: addMonths(new Date(), 6).toISOString(),
      })
      .then(setSchedules)
      .catch(() => setError('일정 목록을 불러오지 못했습니다.'));

  useEffect(() => {
    loadGames();
    loadSchedules();
  }, []);

  // ----- 게임 CRUD -----

  const submitGame = async (event) => {
    event.preventDefault();
    setError(null);
    try {
      if (editingGameId != null) {
        await adminApi.updateGame(editingGameId, gameForm);
      } else {
        await adminApi.createGame(gameForm);
      }
      setGameForm(emptyGameForm);
      setEditingGameId(null);
      await loadGames();
    } catch {
      setError('게임 저장에 실패했습니다.');
    }
  };

  const startEditGame = (game) => {
    setEditingGameId(game.id);
    setGameForm({
      title: game.title ?? '',
      gameCode: game.gameCode ?? '',
      hasGacha: !!game.hasGacha,
      hasPass: !!game.hasPass,
      canRecord: !!game.canRecord,
      isService: !!game.isService,
      comment: game.comment ?? '',
      canManageSchedule: !!game.canManageSchedule,
      canTrackCurrency: !!game.canTrackCurrency,
    });
  };

  const removeGame = async (game) => {
    if (!window.confirm(`'${game.title}' 게임을 삭제할까요?`)) return;
    setError(null);
    try {
      await adminApi.deleteGame(game.id);
      await loadGames();
    } catch {
      setError('게임 삭제에 실패했습니다.');
    }
  };

  // ----- 일정 CRUD -----

  const submitSchedule = async (event) => {
    event.preventDefault();
    setError(null);
    const payload = {
      gameCode: scheduleForm.gameCode,
      title: scheduleForm.title,
      category: scheduleForm.category,
      startAt: toIso(scheduleForm.startAt),
      endAt: toIso(scheduleForm.endAt),
      description: scheduleForm.description || null,
    };
    try {
      if (editingScheduleId != null) {
        await adminApi.updateSchedule(editingScheduleId, payload);
      } else {
        await adminApi.createSchedule(payload);
      }
      setScheduleForm(emptyScheduleForm);
      setEditingScheduleId(null);
      await loadSchedules();
    } catch {
      setError('일정 저장에 실패했습니다.');
    }
  };

  const startEditSchedule = (schedule) => {
    setEditingScheduleId(schedule.id);
    setScheduleForm({
      gameCode: schedule.gameCode ?? '',
      title: schedule.title ?? '',
      category: schedule.category ?? 'EVENT',
      startAt: toLocalInput(schedule.startAt),
      endAt: toLocalInput(schedule.endAt),
      description: schedule.description ?? '',
    });
  };

  const removeSchedule = async (schedule) => {
    if (!window.confirm(`'${schedule.title}' 일정을 삭제할까요?`)) return;
    setError(null);
    try {
      await adminApi.deleteSchedule(schedule.id);
      await loadSchedules();
    } catch {
      setError('일정 삭제에 실패했습니다.');
    }
  };

  return (
    <div className="game-mgmt">
      <h1>게임 관리하기</h1>

      {error && <p className="game-mgmt-error">{error}</p>}

      <section>
        <h2>{editingGameId != null ? '게임 수정' : '게임 등록'}</h2>
        <form className="game-mgmt-form" onSubmit={submitGame}>
          <label>
            게임명
            <input
              type="text"
              required
              value={gameForm.title}
              onChange={(e) => setGameForm({ ...gameForm, title: e.target.value })}
            />
          </label>
          <label>
            게임 코드 (등록 후 변경 불가)
            <input
              type="text"
              required
              disabled={editingGameId != null}
              value={gameForm.gameCode}
              onChange={(e) => setGameForm({ ...gameForm, gameCode: e.target.value })}
            />
          </label>
          <div className="game-mgmt-checkboxes">
            {GAME_FLAGS.map(([key, label]) => (
              <label key={key} className="checkbox">
                <input
                  type="checkbox"
                  checked={gameForm[key]}
                  onChange={(e) => setGameForm({ ...gameForm, [key]: e.target.checked })}
                />
                {label}
              </label>
            ))}
          </div>
          <label className="full-width">
            메모
            <input
              type="text"
              value={gameForm.comment}
              onChange={(e) => setGameForm({ ...gameForm, comment: e.target.value })}
            />
          </label>
          <div className="game-mgmt-actions">
            <button type="submit" className="submit">
              {editingGameId != null ? '수정 저장' : '등록'}
            </button>
            {editingGameId != null && (
              <button
                type="button"
                onClick={() => {
                  setEditingGameId(null);
                  setGameForm(emptyGameForm);
                }}
              >
                취소
              </button>
            )}
          </div>
        </form>

        {games.length === 0 ? (
          <p className="game-mgmt-empty">등록된 게임이 없습니다.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>게임명</th>
                <th>코드</th>
                <th>서비스 중</th>
                <th>일정 관리</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {games.map((game) => (
                <tr key={game.id}>
                  <td>{game.title}</td>
                  <td>{game.gameCode}</td>
                  <td>{game.isService ? 'O' : 'X'}</td>
                  <td>{game.canManageSchedule ? 'O' : 'X'}</td>
                  <td className="row-actions">
                    <button type="button" onClick={() => startEditGame(game)}>수정</button>
                    <button type="button" onClick={() => removeGame(game)}>삭제</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <section>
        <h2>{editingScheduleId != null ? '일정 수정' : '일정 등록'}</h2>
        <form className="game-mgmt-form" onSubmit={submitSchedule}>
          <label>
            게임
            <select
              required
              value={scheduleForm.gameCode}
              onChange={(e) => setScheduleForm({ ...scheduleForm, gameCode: e.target.value })}
            >
              <option value="" disabled>게임 선택</option>
              {games
                .filter((g) => g.canManageSchedule)
                .map((g) => (
                  <option key={g.gameCode} value={g.gameCode}>{g.title}</option>
                ))}
            </select>
          </label>
          <label>
            일정 제목
            <input
              type="text"
              required
              value={scheduleForm.title}
              onChange={(e) => setScheduleForm({ ...scheduleForm, title: e.target.value })}
            />
          </label>
          <label>
            분류
            <select
              value={scheduleForm.category}
              onChange={(e) => setScheduleForm({ ...scheduleForm, category: e.target.value })}
            >
              {Object.entries(CATEGORY_LABELS).map(([key, label]) => (
                <option key={key} value={key}>{label}</option>
              ))}
            </select>
          </label>
          <label>
            시작
            <input
              type="datetime-local"
              required
              value={scheduleForm.startAt}
              onChange={(e) => setScheduleForm({ ...scheduleForm, startAt: e.target.value })}
            />
          </label>
          <label>
            종료 (미정이면 비워두기)
            <input
              type="datetime-local"
              value={scheduleForm.endAt}
              onChange={(e) => setScheduleForm({ ...scheduleForm, endAt: e.target.value })}
            />
          </label>
          <label className="full-width">
            설명
            <input
              type="text"
              value={scheduleForm.description}
              onChange={(e) => setScheduleForm({ ...scheduleForm, description: e.target.value })}
            />
          </label>
          <div className="game-mgmt-actions">
            <button type="submit" className="submit">
              {editingScheduleId != null ? '수정 저장' : '등록'}
            </button>
            {editingScheduleId != null && (
              <button
                type="button"
                onClick={() => {
                  setEditingScheduleId(null);
                  setScheduleForm(emptyScheduleForm);
                }}
              >
                취소
              </button>
            )}
          </div>
        </form>

        {schedules.length === 0 ? (
          <p className="game-mgmt-empty">최근 -1개월 ~ +6개월 범위에 일정이 없습니다.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>게임</th>
                <th>제목</th>
                <th>분류</th>
                <th>시작</th>
                <th>종료</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {schedules.map((schedule) => (
                <tr key={schedule.id}>
                  <td>{schedule.gameTitle ?? schedule.gameCode}</td>
                  <td>{schedule.title}</td>
                  <td>{CATEGORY_LABELS[schedule.category] ?? schedule.category}</td>
                  <td>{toLocalInput(schedule.startAt).replace('T', ' ')}</td>
                  <td>{schedule.endAt ? toLocalInput(schedule.endAt).replace('T', ' ') : '미정'}</td>
                  <td className="row-actions">
                    <button type="button" onClick={() => startEditSchedule(schedule)}>수정</button>
                    <button type="button" onClick={() => removeSchedule(schedule)}>삭제</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}

export default GameManagementPage;
