import React, { useState, useCallback, useEffect } from "react";
import {
  User,
  Mail,
  Save,
  X,
  Trash2,
  Shield,
  LogOut,
  Users,
} from "lucide-react";
import { showError, showSuccess, showWarning } from "./Notification/NotificationSystem";
import LoadingAnimation from "./Loading/LoadingAnimation";
import { useApis } from "../api-client/api";
import { useAuth } from "../auth/AuthProvider";
import "../styles/SettingsPage.css";

export default function SettingsPage() {
  const { user, logout, refreshUser } = useAuth();
  const apis = useApis();
  const { userApi } = apis || {};

  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(false);

  const [formData, setFormData] = useState({
    name: user?.name || "",
    surname: user?.surname || "",
  });
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showOwnershipModal, setShowOwnershipModal] = useState(false);
  const [deleteConfirmText, setDeleteConfirmText] = useState("");

  const [ownedGroups, setOwnedGroups] = useState([]);
  const [ownershipTransfers, setOwnershipTransfers] = useState({});
  const [members, setMembers] = useState({});

  useEffect(() => {
    setFormData({ name: user?.name || "", surname: user?.surname || "" });
  }, [user]);

  const checkGroupOwnership = useCallback(async () => {
    if (!userApi) return { hasGroups: false, groups: [] };

    try {
      const response = await userApi.getGroups();
      const groups =
        response?.data?.groups ||
        response?.data?.[0]?.groups ||
        response?.data ||
        [];
      const normalized = Array.isArray(groups) ? groups : [];
      return { hasGroups: normalized.length > 0, groups: normalized };
    } catch (error) {
      console.error("Error checking group ownership:", error);
      return { hasGroups: false, groups: [] };
    }
  }, [userApi]);

  const loadGroupMembers = useCallback(
    async (groupId) => {
      if (!userApi) return [];
      try {
        const response = await userApi.getMembers(groupId);
        const users = response?.data?.users || response?.data || [];
        return users.filter((m) => m?.user?.userId !== user?.userId);
      } catch (error) {
        console.error(`Error loading members for group ${groupId}:`, error);
        return [];
      }
    },
    [userApi, user]
  );

  const handleSaveProfile = useCallback(async () => {
    if (!userApi) return;
    if (!formData.name.trim()) {
      showError("Имя не может быть пустым");
      return;
    }
    if (!formData.surname.trim()) {
      showError("Фамилия не может быть пустой");
      return;
    }

    setLoading(true);
    try {
      await userApi.updateUser({
        name: formData.name.trim(),
        surname: formData.surname.trim(),
      });
      showSuccess("Профиль успешно обновлён");
      setEditing(false);
      if (refreshUser) await refreshUser();
    } catch (error) {
      console.error("Error updating profile:", error);
      showError("Не удалось обновить профиль");
    } finally {
      setLoading(false);
    }
  }, [userApi, formData, refreshUser]);

  const handleCancelEdit = useCallback(() => {
    setFormData({ name: user?.name || "", surname: user?.surname || "" });
    setEditing(false);
  }, [user]);

  const handleInitiateDelete = useCallback(async () => {
    if (!userApi) return;
    setLoading(true);
    try {
      const { hasGroups, groups } = await checkGroupOwnership();
      if (!hasGroups) {
        setShowDeleteModal(true);
        return;
      }

      const membersResults = await Promise.all(groups.map((g) => loadGroupMembers(g.id)));
      const membersData = {};
      groups.forEach((g, idx) => {
        membersData[g.id] = membersResults[idx] || [];
      });

      setOwnedGroups(groups);
      setMembers(membersData);

      setOwnershipTransfers((prev) => {
        const next = {};
        groups.forEach((g) => {
          if (prev[g.id]) next[g.id] = prev[g.id];
        });
        return next;
      });

      const groupsNeedingTransfer = groups.filter((g) => (membersData[g.id] || []).length > 0);
      if (groupsNeedingTransfer.length === 0) {
        setShowDeleteModal(true);
      } else {
        setShowOwnershipModal(true);
      }
    } catch (error) {
      console.error("Error initiating delete:", error);
      showError("Ошибка при проверке владения группами");
    } finally {
      setLoading(false);
    }
  }, [checkGroupOwnership, loadGroupMembers, userApi]);

  const handleSelectNewOwner = useCallback((groupId, userId) => {
    setOwnershipTransfers((prev) => ({ ...prev, [groupId]: userId }));
  }, []);

  const sendOwnershipTransfers = useCallback(async () => {
    if (!userApi) return { success: false };

    const groupsWithMembers = ownedGroups.filter((g) => (members[g.id] || []).length > 0);
    const missing = groupsWithMembers.filter((g) => !ownershipTransfers[g.id]);
    if (missing.length > 0) {
      showError("Выберите новых владельцев для всех групп, где это требуется");
      return { success: false };
    }

    const groupOwnershipChanges = groupsWithMembers.map((g) => ({
      userId: ownershipTransfers[g.id],
      groupId: g.id,
    }));

    if (groupOwnershipChanges.length === 0) return { success: true };

    setLoading(true);
    try {
      await userApi.changesOwnerGroups({ groupOwnershipChanges });
      showSuccess("Права владения успешно переданы");
      return { success: true };
    } catch (error) {
      console.error("Error transferring ownership:", error);
      showError("Не удалось передать права владения");
      return { success: false };
    } finally {
      setLoading(false);
    }
  }, [userApi, ownedGroups, ownershipTransfers, members]);

  const handleTransferOwnership = useCallback(async () => {
    const result = await sendOwnershipTransfers();
    if (result.success) {
      setShowOwnershipModal(false);
      setShowDeleteModal(true);
    }
  }, [sendOwnershipTransfers]);

  const handleTransferAndDelete = useCallback(async () => {
    if (!userApi) return;
    const transferResult = await sendOwnershipTransfers();
    if (!transferResult.success) return;

    setLoading(true);
    try {
      await userApi.deleteUser();
      showWarning("Аккаунт удалён");
      setTimeout(() => { if (logout) logout(); }, 1200);
    } catch (error) {
      console.error("Error deleting account:", error);
      if (error?.response?.data?.details?.groupsId) {
        const groupsId = error.response.data.details.groupsId;
        showError(`Не удалось удалить аккаунт. Группы ID: ${groupsId.join(", ")} имеют участников.`);
      } else {
        showError("Не удалось удалить аккаунт");
      }
    } finally {
      setLoading(false);
    }
  }, [sendOwnershipTransfers, userApi, logout]);

  const handleDeleteAccount = useCallback(async () => {
    if (!userApi) return;
    const confirmPhrase = user?.email || "DELETE";
    if (deleteConfirmText !== confirmPhrase) {
      showError("Неверный текст подтверждения");
      return;
    }

    setLoading(true);
    try {
      await userApi.deleteUser();
      showWarning("Аккаунт удалён");
      setTimeout(() => { if (logout) logout(); }, 1200);
    } catch (error) {
      console.error("Error deleting account:", error);
      if (error?.response?.data?.details?.groupsId) {
        const groupsId = error.response.data.details.groupsId;
        showError(`Не удалось удалить аккаунт. Группы ID: ${groupsId.join(", ")} имеют участников.`);
      } else {
        showError("Не удалось удалить аккаунт");
      }
    } finally {
      setLoading(false);
    }
  }, [userApi, deleteConfirmText, user, logout]);

  const handleInputChange = useCallback((field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  }, []);

  if (loading && !editing && !showOwnershipModal && !showDeleteModal) {
    return <LoadingAnimation message="Обработка запроса..." />;
  }

  const groupsNeedingTransferCount = ownedGroups.filter((g) => (members[g.id] || []).length > 0).length;

  return (
    <div className="settings-page">
      <div className="settings-container">
        <section className="settings-card">
          <div className="settings-card-header">
            <div className="settings-card-title">
              <User size={24} />
              <h2>Личные данные</h2>
            </div>
            {!editing && (
              <button className="btn btn-secondary" onClick={() => setEditing(true)}>Редактировать</button>
            )}
          </div>
          <div className="settings-card-body">
            {editing ? (
              <div className="settings-form">
                <div className="form-group">
                  <label className="form-label"><User size={18} /> Имя</label>
                  <input className="form-input" value={formData.name} onChange={(e) => handleInputChange("name", e.target.value)} maxLength={50} />
                </div>
                <div className="form-group">
                  <label className="form-label"><User size={18} /> Фамилия</label>
                  <input className="form-input" value={formData.surname} onChange={(e) => handleInputChange("surname", e.target.value)} maxLength={50} />
                </div>
                <div className="form-actions">
                  <button className="btn btn-primary" onClick={handleSaveProfile} disabled={loading}><Save size={18} /> {loading ? "Сохранение..." : "Сохранить"}</button>
                  <button className="btn btn-secondary" onClick={handleCancelEdit} disabled={loading}><X size={18} /> Отмена</button>
                </div>
              </div>
            ) : (
              <div className="profile-info">
                <div className="profile-avatar-section">
                  <div className="profile-avatar-large">{`${user?.name?.[0] || ""}${user?.surname?.[0] || ""}`.toUpperCase()}</div>
                  <div className="profile-avatar-info">
                    <h3>{user?.name} {user?.surname}</h3>
                    <p className="profile-email"><Mail size={14} /> {user?.email}</p>
                  </div>
                </div>
                <div className="profile-details">
                  <div className="profile-detail-item"><span className="detail-label">Имя:</span><span className="detail-value">{user?.name}</span></div>
                  <div className="profile-detail-item"><span className="detail-label">Фамилия:</span><span className="detail-value">{user?.surname}</span></div>
                  <div className="profile-detail-item"><span className="detail-label">Email:</span><span className="detail-value">{user?.email}</span></div>
                </div>
              </div>
            )}
          </div>
        </section>
        <section className="settings-card">
          <div className="settings-card-header">
            <div className="settings-card-title"><Shield size={24} /><h2>Безопасность</h2></div>
          </div>
          <div className="settings-card-body">
            <div className="security-actions">
              <div className="security-item">
                <div className="security-item-icon"><LogOut size={20} /></div>
                <div className="security-item-content">
                  <h3>Выйти из аккаунта</h3><p>Завершить текущий сеанс работы</p>
                </div>
                <button className="btn btn-secondary" onClick={() => { if (logout) logout(); }}>Выйти</button>
              </div>
            </div>
          </div>
        </section>
        <section className="settings-card danger-zone">
          <div className="settings-card-header">
            <div className="settings-card-title"><Trash2 size={24} /><h2>Опасная зона</h2></div>
          </div>
          <div className="settings-card-body">
            <div className="danger-warning">
              <p><strong>Внимание!</strong> Удаление аккаунта — необратимое действие. Все ваши данные будут безвозвратно удалены.</p>
            </div>
            <button className="btn btn-danger" onClick={handleInitiateDelete} disabled={loading}><Trash2 size={16} /> {loading ? "Проверка..." : "Удалить аккаунт"}</button>
          </div>
        </section>
      </div>

      {showOwnershipModal && (
        <div className="transfer-modal-overlay" onClick={() => setShowOwnershipModal(false)}>
          <div className="transfer-modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="transfer-modal-head">
              <div className="transfer-modal-icon"><Users size={26} /></div>
              <div>
                <h3 className="transfer-modal-title">Передача прав владения</h3>
                <div className="transfer-modal-sub">Вы владеете <strong>{ownedGroups.length}</strong> группами. Для удаления необходимо передать права владельцам в группах, где есть другие участники.</div>
              </div>
            </div>

            <div className="transfer-body">
              <div className="transfer-list">
                {ownedGroups.map((group) => {
                  const groupMembers = members[group.id] || [];
                  return (
                    <div key={group.id} className="transfer-item">
                      <div className="transfer-group-avatar">{group.name?.[0]?.toUpperCase() || "G"}</div>
                      <div className="transfer-group-info">
                        <div className="transfer-group-name">{group.name}</div>
                        <div className="transfer-group-meta">{groupMembers.length} участник(ов)</div>

                        {groupMembers.length > 0 && (
                          <div className="transfer-members">
                            {groupMembers.map((member) => {
                              const isSelected = ownershipTransfers[group.id] === member.user.userId;
                              return (
                                <div
                                  key={member.user.userId}
                                  className={`transfer-member ${isSelected ? "selected" : ""}`}
                                  onClick={() => handleSelectNewOwner(group.id, member.user.userId)}
                                >
                                  <div className="member-avatar">{`${member.user.name?.[0] || ""}${member.user.surname?.[0] || ""}`.toUpperCase()}</div>
                                  <div className="member-info">
                                    <div className="member-name">{member.user.name} {member.user.surname}</div>
                                    <div className="member-email">{member.user.email}</div>
                                  </div>
                                  {isSelected && <div className="selected-badge">Выбран</div>}
                                </div>
                              );
                            })}
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>

              <div className="transfer-modal-actions">
                <button className="btn-ghost" onClick={() => setShowOwnershipModal(false)}>Отмена</button>

                <button
                  className="btn-primary-gradient"
                  onClick={handleTransferOwnership}
                  disabled={groupsNeedingTransferCount !== Object.keys(ownershipTransfers).length || loading}
                  title="Передать права и продолжить"
                >
                  Передать права и продолжить
                </button>

                <button
                  className="btn-primary-gradient"
                  onClick={handleTransferAndDelete}
                  disabled={groupsNeedingTransferCount !== Object.keys(ownershipTransfers).length || loading}
                  title="Передать права и сразу удалить аккаунт"
                >
                  Передать и удалить аккаунт
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {showDeleteModal && (
        <div className="modal-overlay" onClick={() => setShowDeleteModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Подтверждение удаления</h2>
              <button className="modal-close" onClick={() => setShowDeleteModal(false)}><X size={18} /></button>
            </div>
            <div className="modal-body">
              <div className="modal-warning">
                <Trash2 size={48} />
                <p>Вы уверены, что хотите <strong>безвозвратно</strong> удалить свой аккаунт?</p>
                <p className="modal-warning-text">Это действие нельзя отменить. Все ваши данные будут удалены.</p>
              </div>

              <div className="form-group">
                <label className="form-label">Для подтверждения введите: <strong>{user?.email}</strong></label>
                <input className="form-input" value={deleteConfirmText} onChange={(e) => setDeleteConfirmText(e.target.value)} placeholder="Введите ваш email" />
              </div>
            </div>

            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => { setShowDeleteModal(false); setDeleteConfirmText(""); }}>Отмена</button>
              <button className="btn btn-danger" onClick={handleDeleteAccount} disabled={loading || deleteConfirmText !== (user?.email || "DELETE")}>
                <Trash2 size={16} /> {loading ? "Удаление..." : "Удалить аккаунт"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
