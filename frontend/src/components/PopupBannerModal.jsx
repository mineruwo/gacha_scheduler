import React, { useEffect, useState } from 'react';
import { announcementApi } from '../api/announcementApi';
import './PopupBannerModal.css';

const DISMISS_KEY = 'dismissedPopups';

function todayString() {
  return new Date().toISOString().slice(0, 10);
}

function readDismissed() {
  try {
    return JSON.parse(localStorage.getItem(DISMISS_KEY)) ?? {};
  } catch {
    return {};
  }
}

function PopupBannerModal() {
  const [popup, setPopup] = useState(null);

  useEffect(() => {
    announcementApi
      .fetchActive('POPUP')
      .then((list) => {
        const dismissed = readDismissed();
        const next = list.find((p) => dismissed[p.id] !== todayString());
        if (next) setPopup(next);
      })
      .catch(() => {});
  }, []);

  if (!popup) return null;

  const close = () => setPopup(null);

  const dismissToday = () => {
    const dismissed = readDismissed();
    dismissed[popup.id] = todayString();
    localStorage.setItem(DISMISS_KEY, JSON.stringify(dismissed));
    close();
  };

  return (
    <div className="popup-banner-overlay" onClick={close}>
      <div className="popup-banner" onClick={(e) => e.stopPropagation()}>
        <button type="button" className="popup-banner-close" onClick={close} aria-label="닫기">
          ×
        </button>
        {popup.linkUrl ? (
          <a href={popup.linkUrl}>
            {popup.imageUrl && <img src={popup.imageUrl} alt={popup.title} />}
          </a>
        ) : (
          popup.imageUrl && <img src={popup.imageUrl} alt={popup.title} />
        )}
        <div className="popup-banner-body">
          <h3>{popup.title}</h3>
          <button type="button" className="popup-banner-dismiss" onClick={dismissToday}>
            오늘 하루 보지 않기
          </button>
        </div>
      </div>
    </div>
  );
}

export default PopupBannerModal;
