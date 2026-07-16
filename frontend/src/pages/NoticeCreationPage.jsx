import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { boardApi } from '../api/boardApi';
import './NoticeCreationPage.css';

const TEMPLATES = [
  { value: 'GUIDE', label: '공략', placeholder: '공략 대상, 준비물, 단계별 설명 등을 적어주세요.' },
  { value: 'QUESTION', label: '질문', placeholder: '상황, 시도해본 것, 궁금한 점을 적어주세요.' },
  { value: 'FREE', label: '자유', placeholder: '자유롭게 작성해주세요.' },
];

const emptyForm = { channelId: '', title: '', content: '', templateType: 'GUIDE' };

function NoticeCreationPage() {
  const navigate = useNavigate();
  const [channels, setChannels] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    boardApi
      .fetchChannels()
      .then((data) => {
        setChannels(data);
        if (data.length > 0) setForm((f) => ({ ...f, channelId: data[0].id }));
      })
      .catch(() => setError('채널 목록을 불러오지 못했습니다.'));
  }, []);

  const currentTemplate = TEMPLATES.find((t) => t.value === form.templateType) ?? TEMPLATES[0];

  const submit = async (event) => {
    event.preventDefault();
    if (!form.channelId) {
      setError('채널을 선택해주세요.');
      return;
    }
    setError(null);
    setSubmitting(true);
    try {
      await boardApi.createPost(form.channelId, {
        title: form.title,
        content: form.content,
        templateType: form.templateType,
      });
      navigate('/board');
    } catch {
      setError('글 작성에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="notice-create">
      <h1>글쓰기</h1>

      {error && <p className="notice-create-error">{error}</p>}

      <form className="notice-create-form" onSubmit={submit}>
        <label>
          채널
          <select
            required
            value={form.channelId}
            onChange={(e) => setForm({ ...form, channelId: e.target.value })}
          >
            <option value="" disabled>채널 선택</option>
            {channels.map((ch) => (
              <option key={ch.id} value={ch.id}>
                {ch.gameName ? `${ch.gameName} · ` : ''}{ch.name}
              </option>
            ))}
          </select>
        </label>

        <label>
          템플릿
          <div className="notice-create-templates">
            {TEMPLATES.map((t) => (
              <button
                key={t.value}
                type="button"
                className={form.templateType === t.value ? 'active' : ''}
                onClick={() => setForm({ ...form, templateType: t.value })}
              >
                {t.label}
              </button>
            ))}
          </div>
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

        <label>
          내용
          <textarea
            required
            placeholder={currentTemplate.placeholder}
            value={form.content}
            onChange={(e) => setForm({ ...form, content: e.target.value })}
          />
        </label>

        <div className="notice-create-actions">
          <button type="submit" className="submit" disabled={submitting}>
            {submitting ? '등록 중...' : '등록'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default NoticeCreationPage;
