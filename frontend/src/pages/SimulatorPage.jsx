import React, { useEffect, useState } from 'react';
import { gachaApi } from '../api/gachaApi';
import './SimulatorPage.css';

// 비로그인 유저의 천장 카운트는 배너별로 localStorage에 보관한다.
// 로그인 연동(파트 02) 이후에는 서버가 반환하는 카운트가 우선한다.
const pityKey = (bannerId) => `gacha_pity_${bannerId}`;
const loadPity = (bannerId) => Number(localStorage.getItem(pityKey(bannerId))) || 0;

function rarityClass(rarity) {
  if (rarity >= 5) return 'rarity-top';
  if (rarity === 4) return 'rarity-high';
  if (rarity === 3) return 'rarity-mid';
  return 'rarity-low';
}

function SimulatorPage() {
  const [banners, setBanners] = useState(null);
  const [selectedBanner, setSelectedBanner] = useState(null);
  const [pity, setPity] = useState(0);
  const [results, setResults] = useState([]);
  const [isPulling, setIsPulling] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    gachaApi
      .fetchBanners()
      .then((data) => {
        if (cancelled) return;
        setBanners(data);
        if (data.length > 0) {
          setSelectedBanner(data[0]);
          setPity(loadPity(data[0].id));
        }
      })
      .catch(() => {
        if (!cancelled) setError('배너 목록을 불러오지 못했습니다.');
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const handleBannerChange = (event) => {
    const banner = banners.find((b) => String(b.id) === event.target.value);
    if (!banner) return;
    setSelectedBanner(banner);
    setPity(loadPity(banner.id));
    setResults([]);
    setError(null);
  };

  const handlePull = async (count) => {
    if (!selectedBanner || isPulling) return;
    setIsPulling(true);
    setError(null);
    try {
      const result = await gachaApi.pull(selectedBanner.id, { count, currentPity: pity });
      localStorage.setItem(pityKey(selectedBanner.id), String(result.pityCount));
      setPity(result.pityCount);
      setResults(result.results);
    } catch {
      setError('뽑기에 실패했습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      setIsPulling(false);
    }
  };

  if (banners == null) {
    return (
      <div className="simulator-page">
        <h1>가챠 시뮬레이터</h1>
        {error ? <p className="simulator-error">{error}</p> : <p>배너 목록을 불러오는 중...</p>}
      </div>
    );
  }

  if (banners.length === 0) {
    return (
      <div className="simulator-page">
        <h1>가챠 시뮬레이터</h1>
        <p>진행 중인 배너가 없습니다.</p>
      </div>
    );
  }

  const threshold = selectedBanner?.pityThreshold ?? 0;
  const progress = threshold > 0 ? Math.min(pity / threshold, 1) : 0;

  return (
    <div className="simulator-page">
      <h1>가챠 시뮬레이터</h1>

      <select
        className="simulator-banner-select"
        value={selectedBanner?.id ?? ''}
        onChange={handleBannerChange}
        disabled={isPulling}
      >
        {banners.map((banner) => (
          <option key={banner.id} value={banner.id}>
            [{banner.gameName}] {banner.name}
          </option>
        ))}
      </select>

      <div className="simulator-pity">
        <span>
          천장 카운트: {pity} / {threshold}
        </span>
        <div className="simulator-pity-track">
          <div className="simulator-pity-fill" style={{ width: `${progress * 100}%` }} />
        </div>
      </div>

      <div className="simulator-results">
        {results.length === 0 ? (
          <div className="simulator-results-empty">뽑기 결과가 여기에 표시됩니다</div>
        ) : (
          <div className="simulator-result-grid">
            {results.map((character, index) => (
              <div key={`${index}-${character.id}`} className={`simulator-card ${rarityClass(character.rarity)}`}>
                <span className="simulator-card-name">{character.name}</span>
                <span className="simulator-card-rarity">★{character.rarity}</span>
              </div>
            ))}
          </div>
        )}
      </div>

      {error && <p className="simulator-error">{error}</p>}

      <div className="simulator-actions">
        <button type="button" onClick={() => handlePull(1)} disabled={isPulling}>
          1회 뽑기
        </button>
        <button type="button" className="pull-ten" onClick={() => handlePull(10)} disabled={isPulling}>
          {isPulling ? '뽑는 중...' : '10연 뽑기'}
        </button>
      </div>
    </div>
  );
}

export default SimulatorPage;
