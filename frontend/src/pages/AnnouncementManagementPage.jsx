import React, { useEffect, useState } from 'react';
import { adminAnnouncementApi } from '../api/announcementApi';
import './AnnouncementManagementPage.css';

const TYPE_LABELS = { NOTICE: '공지사항', POPUP: '팝업 배너' };

const emptyForm = {
  type: 'NOTICE',
  title: '',
  content: '',
  imageUrl: '',
  linkUrl: '',
  startAt: '',
  endAt: '',
  isActive: true,
};

// OffsetDateTime(ISO) ↔ <input type="datetime-local"> 값 변환 (다른 관리 페이지와 동일 패턴)
function toLocalInput(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function toIso(local) {
  return local ? new Date(local).toISOString() : null;
}

function AnnouncementManagementPage() {
  const [announcements, setAnnouncements] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState(null);

  const loadAnnouncements = () =>
    adminAnnouncementApi
      .fetchAll()
      .then(setAnnouncements)
      .catch(() => setError('목록을 불러오지 못했습니다.'));

  useEffect(() => {
    loadAnnouncements();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setError(null);
    const payload = {
      type: form.type,
      title: form.title,
      content: form.type === 'NOTICE' ? form.content || null : null,
      imageUrl: form.type === 'POPUP' ? form.imageUrl || null : null,
      linkUrl: form.linkUrl || null,
      startAt: toIso(form.startAt),
      endAt: toIso(form.endAt),
      isActive: form.isActive,
    };
    try {
      if (editingId != null) {
        await adminAnnouncementApi.update(editingId, payload);
      } else {
        await adminAnnouncementApi.create(payload);
      }
      setForm(emptyForm);
      setEditingId(null);
      await loadAnnouncements();
    } catch {
      setError('저장에 실패했습니다.');
    }
  };

  const startEdit = (item) => {
    setEditingId(item.id);
    setForm({
      type: item.type,
      title: item.title ?? '',
      content: item.content ?? '',
      imageUrl: item.imageUrl ?? '',
      linkUrl: item.linkUrl ?? '',
      startAt: toLocalInput(item.startAt),
      endAt: toLocalInput(item.endAt),
      isActive: !!item.isActive,
    });
  };

  const cancelEdit = () => {
    setEditingId(null);
    setForm(emptyForm);
  };

  const remove = async (item) => {
    if (!window.confirm(`'${item.title}'을(를) 삭제할까요?`)) return;
    setError(null);
    try {
      await adminAnnouncementApi.delete(item.id);
      await loadAnnouncements();
    } catch {
      setError('삭제에 실패했습니다.');
    }
  };

  return (
    <div className="announcement-mgmt">
      <h1>공지사항 / 팝업 배너 관리</h1>

      {error && <p className="announcement-mgmt-error">{error}</p>}

      <section>
        <h2>{editingId != null ? '수정' : '등록'}</h2>
        <form className="announcement-mgmt-form" onSubmit={submit}>
          <label>
            종류
            <select
              value={form.type}
              onChange={(e) => setForm({ ...form, type: e.target.value })}
            >
              <option value="NOTICE">공지사항</option>
              <option value="POPUP">팝업 배너</option>
            </select>
          </label>
          <label>
            제목
            <input
              type="text"
              required
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
            />
          </label>
          {form.type === 'NOTICE' ? (
            <label className="full-width">
              내용
              <textarea
                rows={3}
                value={form.content}
                onChange={(e) => setForm({ ...form, content: e.target.value })}
              />
            </label>
          ) : (
            <label className="full-width">
              이미지 URL
              <input
                type="text"
                value={form.imageUrl}
                onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
              />
            </label>
          )}
          <label>
            연결 링크 (선택)
            <input
              type="text"
              value={form.linkUrl}
              onChange={(e) => setForm({ ...form, linkUrl: e.target.value })}
            />
          </label>
          <label>
            시작
            <input
              type="datetime-local"
              required
              value={form.startAt}
              onChange={(e) => setForm({ ...form, startAt: e.target.value })}
            />
          </label>
          <label>
            종료 (미정이면 비워두기)
            <input
              type="datetime-local"
              value={form.endAt}
              onChange={(e) => setForm({ ...form, endAt: e.target.value })}
            />
          </label>
          <label className="checkbox">
            <input
              type="checkbox"
              checked={form.isActive}
              onChange={(e) => setForm({ ...form, isActive: e.target.checked })}
            />
            활성화
          </label>
          <div className="announcement-mgmt-actions">
            <button type="submit" className="submit">{editingId != null ? '수정 저장' : '등록'}</button>
            {editingId != null && <button type="button" onClick={cancelEdit}>취소</button>}
          </div>
        </form>

        {announcements.length === 0 ? (
          <p className="announcement-mgmt-empty">등록된 공지/팝업이 없습니다.</p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>종류</th>
                  <th>제목</th>
                  <th>기간</th>
                  <th>활성</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {announcements.map((item) => (
                  <tr key={item.id}>
                    <td>{TYPE_LABELS[item.type] ?? item.type}</td>
                    <td>{item.title}</td>
                    <td>
                      {toLocalInput(item.startAt).replace('T', ' ')} ~{' '}
                      {item.endAt ? toLocalInput(item.endAt).replace('T', ' ') : '미정'}
                    </td>
                    <td>{item.isActive ? 'O' : 'X'}</td>
                    <td className="row-actions">
                      <button type="button" onClick={() => startEdit(item)}>수정</button>
                      <button type="button" onClick={() => remove(item)}>삭제</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

export default AnnouncementManagementPage;
