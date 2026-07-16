import React, { useEffect, useState } from 'react';
import { schedulerApi } from '../api/schedulerApi';
import { boardApi, adminBoardApi } from '../api/boardApi';
import './ChannelManagementPage.css';

const emptyForm = { gameId: '', name: '', description: '' };

function ChannelManagementPage() {
  const [games, setGames] = useState([]);
  const [channels, setChannels] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState(null);

  const loadChannels = () =>
    boardApi.fetchChannels().then(setChannels).catch(() => setError('채널 목록을 불러오지 못했습니다.'));

  useEffect(() => {
    schedulerApi.fetchGames().then(setGames).catch(() => setError('게임 목록을 불러오지 못했습니다.'));
    loadChannels();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setError(null);
    try {
      if (editingId != null) {
        await adminBoardApi.updateChannel(editingId, form);
      } else {
        await adminBoardApi.createChannel(form);
      }
      setForm(emptyForm);
      setEditingId(null);
      await loadChannels();
    } catch {
      setError('채널 저장에 실패했습니다.');
    }
  };

  const startEdit = (channel) => {
    setEditingId(channel.id);
    setForm({ gameId: channel.gameId ?? '', name: channel.name ?? '', description: channel.description ?? '' });
  };

  const cancelEdit = () => {
    setEditingId(null);
    setForm(emptyForm);
  };

  const remove = async (channel) => {
    if (!window.confirm(`'${channel.name}' 채널을 삭제할까요?`)) return;
    setError(null);
    try {
      await adminBoardApi.deleteChannel(channel.id);
      await loadChannels();
    } catch {
      setError('채널 삭제에 실패했습니다.');
    }
  };

  return (
    <div className="channel-mgmt">
      <h1>채널 관리하기</h1>

      {error && <p className="channel-mgmt-error">{error}</p>}

      <section>
        <h2>{editingId != null ? '채널 수정' : '채널 등록'}</h2>
        <form className="channel-mgmt-form" onSubmit={submit}>
          <label>
            게임
            <select
              required
              disabled={editingId != null}
              value={form.gameId}
              onChange={(e) => setForm({ ...form, gameId: e.target.value })}
            >
              <option value="" disabled>게임 선택</option>
              {games.map((g) => (
                <option key={g.gameCode} value={g.id}>{g.title}</option>
              ))}
            </select>
          </label>
          <label>
            채널명
            <input
              type="text"
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </label>
          <label className="full-width">
            설명
            <input
              type="text"
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </label>
          <div className="channel-mgmt-actions">
            <button type="submit" className="submit">{editingId != null ? '수정 저장' : '등록'}</button>
            {editingId != null && <button type="button" onClick={cancelEdit}>취소</button>}
          </div>
        </form>

        {channels.length === 0 ? (
          <p className="channel-mgmt-empty">등록된 채널이 없습니다.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>게임</th>
                <th>채널명</th>
                <th>설명</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {channels.map((channel) => (
                <tr key={channel.id}>
                  <td>{channel.gameName ?? channel.gameId}</td>
                  <td>{channel.name}</td>
                  <td>{channel.description}</td>
                  <td className="row-actions">
                    <button type="button" onClick={() => startEdit(channel)}>수정</button>
                    <button type="button" onClick={() => remove(channel)}>삭제</button>
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

export default ChannelManagementPage;
