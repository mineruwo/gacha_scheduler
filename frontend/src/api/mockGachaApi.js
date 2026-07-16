// 개발용 Mock. 백엔드 가챠 API(파트 04)가 완성되기 전까지 서버가 수행할
// 가중치/천장 계산을 로컬에서 흉내 낸다. 실서비스 확률 로직은 반드시 서버 책임.
// 샘플 데이터는 frontend_mobile의 MockGachaRepository와 동일하게 유지한다.
const banners = [
  {
    id: 1,
    gameId: 1,
    gameName: '스타레일 (샘플)',
    name: '픽업: 은하의 기사',
    startAt: '2026-07-01T00:00:00',
    endAt: '2026-07-31T23:59:59',
    pickupCharacterIds: [101],
    pityThreshold: 90,
    rateUpRate: 0.5,
  },
  {
    id: 2,
    gameId: 2,
    gameName: '블루 아카이브 (샘플)',
    name: '픽업: 수영복 세나',
    startAt: '2026-07-10T00:00:00',
    endAt: '2026-08-10T23:59:59',
    pickupCharacterIds: [201],
    pityThreshold: 200,
    rateUpRate: 0.7,
  },
];

const charactersByBanner = {
  1: [
    { id: 101, gameId: 1, name: '은하의 기사', rarity: 5, weight: 0.6 },
    { id: 102, gameId: 1, name: '별의 여행자', rarity: 5, weight: 0.6 },
    { id: 103, gameId: 1, name: '유성 사수', rarity: 4, weight: 5.1 },
    { id: 104, gameId: 1, name: '혜성 검사', rarity: 4, weight: 5.1 },
    { id: 105, gameId: 1, name: '위성 정비공', rarity: 3, weight: 44.3 },
    { id: 106, gameId: 1, name: '우주 화물선', rarity: 3, weight: 44.3 },
  ],
  2: [
    { id: 201, gameId: 2, name: '수영복 세나', rarity: 3, weight: 0.7 },
    { id: 202, gameId: 2, name: '체육복 히나', rarity: 3, weight: 0.7 },
    { id: 203, gameId: 2, name: '방과후 유즈', rarity: 2, weight: 18.5 },
    { id: 204, gameId: 2, name: '도서부 코토리', rarity: 2, weight: 18.5 },
    { id: 205, gameId: 2, name: '신입생 모모', rarity: 1, weight: 30.8 },
    { id: 206, gameId: 2, name: '신입생 카에데', rarity: 1, weight: 30.8 },
  ],
};

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

function pickWeighted(pool) {
  const totalWeight = pool.reduce((sum, c) => sum + c.weight, 0);
  let roll = Math.random() * totalWeight;
  for (const c of pool) {
    roll -= c.weight;
    if (roll <= 0) return c;
  }
  return pool[pool.length - 1];
}

function pickHighestRarity(pool, banner, maxRarity) {
  const top = pool.filter((c) => c.rarity === maxRarity);
  const pickup = top.filter((c) => banner.pickupCharacterIds.includes(c.id));
  if (pickup.length > 0 && Math.random() < banner.rateUpRate) {
    return pickup[Math.floor(Math.random() * pickup.length)];
  }
  return pickWeighted(top);
}

export const mockGachaApi = {
  async fetchBanners(gameId) {
    await delay(300);
    if (gameId == null) return banners;
    return banners.filter((b) => b.gameId === gameId);
  },

  async fetchBannerCharacters(bannerId) {
    await delay(200);
    return charactersByBanner[bannerId] ?? [];
  },

  async pull(bannerId, { count, currentPity }) {
    await delay(400);
    const banner = banners.find((b) => b.id === bannerId);
    const pool = charactersByBanner[bannerId];
    if (!banner || !pool?.length) {
      throw new Error(`No characters for banner ${bannerId}`);
    }

    const maxRarity = Math.max(...pool.map((c) => c.rarity));
    const results = [];
    let pity = currentPity;

    for (let i = 0; i < count; i++) {
      pity += 1;
      const picked =
        pity >= banner.pityThreshold
          ? pickHighestRarity(pool, banner, maxRarity) // 확정 천장
          : pickWeighted(pool);
      if (picked.rarity === maxRarity) pity = 0;
      results.push(picked);
    }

    return { results, pityCount: pity };
  },
};
