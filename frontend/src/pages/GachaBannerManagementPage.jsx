import React, { useEffect, useMemo, useState } from 'react';
import { schedulerApi } from '../api/schedulerApi';
import { gachaApi, adminGachaApi } from '../api/gachaApi';
import './GachaBannerManagementPage.css';

const emptyBannerForm = { name: '', startAt: '', endAt: '', pityThreshold: 90, rateUpRate: 0.5 };
const emptyCharacterForm = { name: '', rarity: 5, iconUrl: '' };
const emptyPoolForm = { characterId: '', weight: '', isPickup: false };

// OffsetDateTime(ISO) ↔ <input type="datetime-local"> 값 변환 (GameManagementPage와 동일 패턴)
function toLocalInput(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function toIso(local) {
  return local ? new Date(local).toISOString() : null;
}

function GachaBannerManagementPage() {
  const [games, setGames] = useState([]);
  const [selectedGameId, setSelectedGameId] = useState('');

  const [banners, setBanners] = useState([]);
  const [bannerForm, setBannerForm] = useState(emptyBannerForm);
  const [editingBannerId, setEditingBannerId] = useState(null);

  const [selectedBannerId, setSelectedBannerId] = useState(null);
  const [pool, setPool] = useState([]);

  const [characters, setCharacters] = useState([]);
  const [characterForm, setCharacterForm] = useState(emptyCharacterForm);
  const [editingCharacterId, setEditingCharacterId] = useState(null);

  const [poolForm, setPoolForm] = useState(emptyPoolForm);

  const [error, setError] = useState(null);

  useEffect(() => {
    schedulerApi.fetchGames().then(setGames).catch(() => setError('게임 목록을 불러오지 못했습니다.'));
  }, []);

  const loadBanners = (gameId) =>
    gachaApi
      .fetchBanners(gameId)
      .then((list) => {
        setBanners(list);
        return list;
      })
      .catch(() => setError('배너 목록을 불러오지 못했습니다.'));

  const loadCharacters = (gameId) =>
    adminGachaApi
      .fetchCharactersByGame(gameId)
      .then(setCharacters)
      .catch(() => setError('캐릭터 목록을 불러오지 못했습니다.'));

  const loadPool = (bannerId) =>
    gachaApi
      .fetchBannerCharacters(bannerId)
      .then(setPool)
      .catch(() => setError('배너 풀을 불러오지 못했습니다.'));

  useEffect(() => {
    if (!selectedGameId) {
      setBanners([]);
      setCharacters([]);
      setSelectedBannerId(null);
      return;
    }
    loadBanners(selectedGameId);
    loadCharacters(selectedGameId);
    setSelectedBannerId(null);
    setPool([]);
  }, [selectedGameId]);

  useEffect(() => {
    if (!selectedBannerId) {
      setPool([]);
      return;
    }
    loadPool(selectedBannerId);
  }, [selectedBannerId]);

  const selectedBanner = useMemo(
    () => banners.find((b) => b.id === selectedBannerId) ?? null,
    [banners, selectedBannerId]
  );

  const poolWithPickup = useMemo(
    () =>
      pool.map((entry) => ({
        ...entry,
        isPickup: selectedBanner?.pickupCharacterIds?.includes(entry.id) ?? false,
      })),
    [pool, selectedBanner]
  );

  // 배너 화면(BannerService.pull)과 동일한 규칙으로 자연 확률(천장 적용 전) 미리보기 계산
  const probabilityPreview = useMemo(() => {
    if (poolWithPickup.length === 0) return null;
    const totalWeight = poolWithPickup.reduce((sum, c) => sum + (c.weight ?? 0), 0);
    if (totalWeight <= 0) return null;
    const byRarity = new Map();
    poolWithPickup.forEach((c) => {
      byRarity.set(c.rarity, (byRarity.get(c.rarity) ?? 0) + (c.weight ?? 0));
    });
    const rows = [...byRarity.entries()]
      .sort((a, b) => b[0] - a[0])
      .map(([rarity, weight]) => ({ rarity, weight, ratio: weight / totalWeight }));
    return { totalWeight, rows };
  }, [poolWithPickup]);

  // ----- 배너 CRUD -----

  const submitBanner = async (event) => {
    event.preventDefault();
    setError(null);
    const payload = {
      gameId: Number(selectedGameId),
      name: bannerForm.name,
      startAt: toIso(bannerForm.startAt),
      endAt: toIso(bannerForm.endAt),
      pityThreshold: Number(bannerForm.pityThreshold),
      rateUpRate: Number(bannerForm.rateUpRate),
    };
    try {
      if (editingBannerId != null) {
        await adminGachaApi.updateBanner(editingBannerId, payload);
      } else {
        await adminGachaApi.createBanner(payload);
      }
      setBannerForm(emptyBannerForm);
      setEditingBannerId(null);
      await loadBanners(selectedGameId);
    } catch {
      setError('배너 저장에 실패했습니다.');
    }
  };

  const startEditBanner = (banner) => {
    setEditingBannerId(banner.id);
    setBannerForm({
      name: banner.name ?? '',
      startAt: toLocalInput(banner.startAt),
      endAt: toLocalInput(banner.endAt),
      pityThreshold: banner.pityThreshold ?? 90,
      rateUpRate: banner.rateUpRate ?? 0.5,
    });
  };

  const cancelEditBanner = () => {
    setEditingBannerId(null);
    setBannerForm(emptyBannerForm);
  };

  const removeBanner = async (banner) => {
    if (!window.confirm(`'${banner.name}' 배너를 삭제할까요?`)) return;
    setError(null);
    try {
      await adminGachaApi.deleteBanner(banner.id);
      if (selectedBannerId === banner.id) setSelectedBannerId(null);
      await loadBanners(selectedGameId);
    } catch {
      setError('배너 삭제에 실패했습니다.');
    }
  };

  // ----- 캐릭터 CRUD -----

  const submitCharacter = async (event) => {
    event.preventDefault();
    setError(null);
    const payload = {
      gameId: Number(selectedGameId),
      name: characterForm.name,
      rarity: Number(characterForm.rarity),
      iconUrl: characterForm.iconUrl || null,
    };
    try {
      if (editingCharacterId != null) {
        await adminGachaApi.updateCharacter(editingCharacterId, payload);
      } else {
        await adminGachaApi.createCharacter(payload);
      }
      setCharacterForm(emptyCharacterForm);
      setEditingCharacterId(null);
      await loadCharacters(selectedGameId);
    } catch {
      setError('캐릭터 저장에 실패했습니다.');
    }
  };

  const startEditCharacter = (character) => {
    setEditingCharacterId(character.id);
    setCharacterForm({
      name: character.name ?? '',
      rarity: character.rarity ?? 5,
      iconUrl: character.iconUrl ?? '',
    });
  };

  const cancelEditCharacter = () => {
    setEditingCharacterId(null);
    setCharacterForm(emptyCharacterForm);
  };

  const removeCharacter = async (character) => {
    if (!window.confirm(`'${character.name}' 캐릭터를 삭제할까요? (배너 풀에 포함돼 있으면 실패할 수 있습니다)`)) return;
    setError(null);
    try {
      await adminGachaApi.deleteCharacter(character.id);
      await loadCharacters(selectedGameId);
    } catch {
      setError('캐릭터 삭제에 실패했습니다.');
    }
  };

  // ----- 드랍테이블(풀) 관리 -----

  const charactersNotInPool = useMemo(
    () => characters.filter((c) => !pool.some((p) => p.id === c.id)),
    [characters, pool]
  );

  const submitPoolEntry = async (event) => {
    event.preventDefault();
    if (!selectedBannerId) return;
    setError(null);
    try {
      await adminGachaApi.setPoolCharacter(selectedBannerId, {
        characterId: Number(poolForm.characterId),
        weight: Number(poolForm.weight),
        isPickup: poolForm.isPickup,
      });
      setPoolForm(emptyPoolForm);
      await Promise.all([loadPool(selectedBannerId), loadBanners(selectedGameId)]);
    } catch {
      setError('드랍테이블 저장에 실패했습니다.');
    }
  };

  const updatePoolWeight = async (entry, weight, isPickup) => {
    setError(null);
    try {
      await adminGachaApi.setPoolCharacter(selectedBannerId, {
        characterId: entry.id,
        weight: Number(weight),
        isPickup,
      });
      await Promise.all([loadPool(selectedBannerId), loadBanners(selectedGameId)]);
    } catch {
      setError('가중치 수정에 실패했습니다.');
    }
  };

  const removeFromPool = async (entry) => {
    if (!window.confirm(`'${entry.name}'을(를) 드랍테이블에서 제외할까요?`)) return;
    setError(null);
    try {
      await adminGachaApi.removePoolCharacter(selectedBannerId, entry.id);
      await Promise.all([loadPool(selectedBannerId), loadBanners(selectedGameId)]);
    } catch {
      setError('드랍테이블에서 제외하지 못했습니다.');
    }
  };

  return (
    <div className="gacha-mgmt">
      <h1>가챠 배너 / 드랍테이블 관리</h1>

      {error && <p className="gacha-mgmt-error">{error}</p>}

      <section>
        <h2>게임 선택</h2>
        <select value={selectedGameId} onChange={(e) => setSelectedGameId(e.target.value)}>
          <option value="">게임을 선택하세요</option>
          {games
            .filter((g) => g.hasGacha)
            .map((g) => (
              <option key={g.gameCode} value={g.id}>{g.title}</option>
            ))}
        </select>
      </section>

      {selectedGameId && (
        <>
          <section>
            <h2>{editingBannerId != null ? '배너 수정' : '배너 등록'}</h2>
            <form className="gacha-mgmt-form" onSubmit={submitBanner}>
              <label>
                배너명
                <input
                  type="text"
                  required
                  value={bannerForm.name}
                  onChange={(e) => setBannerForm({ ...bannerForm, name: e.target.value })}
                />
              </label>
              <label>
                시작
                <input
                  type="datetime-local"
                  required
                  value={bannerForm.startAt}
                  onChange={(e) => setBannerForm({ ...bannerForm, startAt: e.target.value })}
                />
              </label>
              <label>
                종료
                <input
                  type="datetime-local"
                  required
                  value={bannerForm.endAt}
                  onChange={(e) => setBannerForm({ ...bannerForm, endAt: e.target.value })}
                />
              </label>
              <label>
                천장 횟수
                <input
                  type="number"
                  min="1"
                  required
                  value={bannerForm.pityThreshold}
                  onChange={(e) => setBannerForm({ ...bannerForm, pityThreshold: e.target.value })}
                />
              </label>
              <label>
                픽업 확률 (0~1, 최고 등급 적중 시 픽업으로 갈 확률)
                <input
                  type="number"
                  min="0"
                  max="1"
                  step="0.01"
                  required
                  value={bannerForm.rateUpRate}
                  onChange={(e) => setBannerForm({ ...bannerForm, rateUpRate: e.target.value })}
                />
              </label>
              <div className="gacha-mgmt-actions">
                <button type="submit" className="submit">
                  {editingBannerId != null ? '수정 저장' : '등록'}
                </button>
                {editingBannerId != null && <button type="button" onClick={cancelEditBanner}>취소</button>}
              </div>
            </form>

            {banners.length === 0 ? (
              <p className="gacha-mgmt-empty">등록된 배너가 없습니다.</p>
            ) : (
              <div className="table-scroll">
                <table>
                  <thead>
                    <tr>
                      <th>배너명</th>
                      <th>기간</th>
                      <th>천장</th>
                      <th>픽업 확률</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {banners.map((banner) => (
                      <tr
                        key={banner.id}
                        className={banner.id === selectedBannerId ? 'selected-row' : ''}
                        onClick={() => setSelectedBannerId(banner.id)}
                      >
                        <td>{banner.name}</td>
                        <td>
                          {toLocalInput(banner.startAt).replace('T', ' ')} ~{' '}
                          {toLocalInput(banner.endAt).replace('T', ' ')}
                        </td>
                        <td>{banner.pityThreshold}</td>
                        <td>{Math.round(banner.rateUpRate * 100)}%</td>
                        <td className="row-actions">
                          <button type="button" onClick={(e) => { e.stopPropagation(); startEditBanner(banner); }}>수정</button>
                          <button type="button" onClick={(e) => { e.stopPropagation(); removeBanner(banner); }}>삭제</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <section>
            <h2>{editingCharacterId != null ? '캐릭터 수정' : '캐릭터 등록'}</h2>
            <p className="gacha-mgmt-hint">드랍테이블에 넣을 캐릭터를 먼저 등록하세요. (이미 등록된 캐릭터는 아래 드랍테이블 섹션에서 배너에 추가합니다)</p>
            <form className="gacha-mgmt-form" onSubmit={submitCharacter}>
              <label>
                캐릭터명
                <input
                  type="text"
                  required
                  value={characterForm.name}
                  onChange={(e) => setCharacterForm({ ...characterForm, name: e.target.value })}
                />
              </label>
              <label>
                등급 (rarity)
                <input
                  type="number"
                  min="1"
                  max="5"
                  required
                  value={characterForm.rarity}
                  onChange={(e) => setCharacterForm({ ...characterForm, rarity: e.target.value })}
                />
              </label>
              <label className="full-width">
                아이콘 URL
                <input
                  type="text"
                  value={characterForm.iconUrl}
                  onChange={(e) => setCharacterForm({ ...characterForm, iconUrl: e.target.value })}
                />
              </label>
              <div className="gacha-mgmt-actions">
                <button type="submit" className="submit">
                  {editingCharacterId != null ? '수정 저장' : '등록'}
                </button>
                {editingCharacterId != null && <button type="button" onClick={cancelEditCharacter}>취소</button>}
              </div>
            </form>

            {characters.length === 0 ? (
              <p className="gacha-mgmt-empty">등록된 캐릭터가 없습니다.</p>
            ) : (
              <div className="table-scroll">
                <table>
                  <thead>
                    <tr>
                      <th>이름</th>
                      <th>등급</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {characters.map((character) => (
                      <tr key={character.id}>
                        <td>{character.name}</td>
                        <td>★{character.rarity}</td>
                        <td className="row-actions">
                          <button type="button" onClick={() => startEditCharacter(character)}>수정</button>
                          <button type="button" onClick={() => removeCharacter(character)}>삭제</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          {selectedBanner && (
            <section>
              <h2>'{selectedBanner.name}' 드랍테이블</h2>

              {probabilityPreview && (
                <div className="gacha-mgmt-preview">
                  <p className="gacha-mgmt-hint">현재 가중치 기준 자연 확률 미리보기 (천장 적용 전, 등급별 합산)</p>
                  <table>
                    <thead>
                      <tr>
                        <th>등급</th>
                        <th>가중치 합</th>
                        <th>확률</th>
                      </tr>
                    </thead>
                    <tbody>
                      {probabilityPreview.rows.map((row) => (
                        <tr key={row.rarity}>
                          <td>★{row.rarity}</td>
                          <td>{row.weight.toFixed(3)}</td>
                          <td>{(row.ratio * 100).toFixed(2)}%</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  {Math.abs(probabilityPreview.totalWeight - 1) > 0.05 && (
                    <p className="gacha-mgmt-warning">
                      풀 전체 가중치 합이 {probabilityPreview.totalWeight.toFixed(3)}입니다. weight는 절대 확률처럼
                      취급되므로(예: 0.006 = 0.6%) 합계가 1.0에 가까워야 등급별 확률이 의도대로 나옵니다. 특정
                      등급이 과도하게 잘 나온다면 다른 등급(특히 하위 등급)의 가중치 합을 늘려 보세요.
                    </p>
                  )}
                </div>
              )}

              {poolWithPickup.length === 0 ? (
                <p className="gacha-mgmt-empty">드랍테이블이 비어 있습니다. 아래에서 캐릭터를 추가하세요.</p>
              ) : (
                <div className="table-scroll">
                  <table>
                    <thead>
                      <tr>
                        <th>캐릭터</th>
                        <th>등급</th>
                        <th>가중치</th>
                        <th>픽업</th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody>
                      {poolWithPickup.map((entry) => (
                        <tr key={entry.id}>
                          <td>{entry.name}</td>
                          <td>★{entry.rarity}</td>
                          <td>
                            <input
                              type="number"
                              min="0"
                              step="0.001"
                              defaultValue={entry.weight}
                              onBlur={(e) => {
                                if (Number(e.target.value) !== entry.weight) {
                                  updatePoolWeight(entry, e.target.value, entry.isPickup);
                                }
                              }}
                            />
                          </td>
                          <td>
                            <input
                              type="checkbox"
                              checked={entry.isPickup}
                              onChange={(e) => updatePoolWeight(entry, entry.weight, e.target.checked)}
                            />
                          </td>
                          <td className="row-actions">
                            <button type="button" onClick={() => removeFromPool(entry)}>제외</button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}

              <form className="gacha-mgmt-form" onSubmit={submitPoolEntry}>
                <label>
                  캐릭터 추가
                  <select
                    required
                    value={poolForm.characterId}
                    onChange={(e) => setPoolForm({ ...poolForm, characterId: e.target.value })}
                  >
                    <option value="" disabled>캐릭터 선택</option>
                    {charactersNotInPool.map((c) => (
                      <option key={c.id} value={c.id}>{c.name} (★{c.rarity})</option>
                    ))}
                  </select>
                </label>
                <label>
                  가중치
                  <input
                    type="number"
                    min="0"
                    step="0.001"
                    required
                    value={poolForm.weight}
                    onChange={(e) => setPoolForm({ ...poolForm, weight: e.target.value })}
                  />
                </label>
                <label className="checkbox">
                  <input
                    type="checkbox"
                    checked={poolForm.isPickup}
                    onChange={(e) => setPoolForm({ ...poolForm, isPickup: e.target.checked })}
                  />
                  픽업
                </label>
                <div className="gacha-mgmt-actions">
                  <button type="submit" className="submit">드랍테이블에 추가</button>
                </div>
              </form>
            </section>
          )}
        </>
      )}
    </div>
  );
}

export default GachaBannerManagementPage;
