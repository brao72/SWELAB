import { useState, useEffect } from 'react'
import { members } from '../api'

export default function Members() {
  const [memberList, setMemberList] = useState([])
  const [showAdd, setShowAdd] = useState(false)
  const [form, setForm] = useState({ type: 'STUDENT', name: '', email: '', phone: '' })
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  useEffect(() => { loadMembers() }, [])

  async function loadMembers() {
    try {
      setMemberList(await members.list())
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleAdd(e) {
    e.preventDefault()
    setError('')
    setMsg('')
    try {
      const result = await members.register(form)
      setMsg(`Member registered! ID: ${result.id}`)
      setForm({ type: 'STUDENT', name: '', email: '', phone: '' })
      setShowAdd(false)
      loadMembers()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleDeactivate(id) {
    if (!confirm('Deactivate this member?')) return
    try {
      await members.deactivate(id)
      loadMembers()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h2>Members</h2>
        <button className="btn btn-primary" onClick={() => setShowAdd(!showAdd)}>
          {showAdd ? 'Cancel' : '+ Register Member'}
        </button>
      </div>

      {error && <div className="error-msg">{error}</div>}
      {msg && <div className="success-msg">{msg}</div>}

      {showAdd && (
        <form className="card form-card" onSubmit={handleAdd}>
          <h3>Register New Member</h3>
          <div className="form-row">
            <div className="form-group">
              <label>Type</label>
              <select value={form.type} onChange={e => setForm({...form, type: e.target.value})}>
                <option value="STUDENT">Student</option>
                <option value="FACULTY">Faculty</option>
              </select>
            </div>
            <div className="form-group">
              <label>Name</label>
              <input value={form.name} onChange={e => setForm({...form, name: e.target.value})} required />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Email</label>
              <input type="email" value={form.email} onChange={e => setForm({...form, email: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Phone</label>
              <input value={form.phone} onChange={e => setForm({...form, phone: e.target.value})} />
            </div>
          </div>
          <button type="submit" className="btn btn-primary">Register</button>
        </form>
      )}

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Type</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {memberList.length === 0 ? (
              <tr><td colSpan="7" className="empty">No members found</td></tr>
            ) : (
              memberList.map(m => (
                <tr key={m.id}>
                  <td>{m.id}</td>
                  <td>{m.name}</td>
                  <td><span className={`badge ${m.memberType === 'FACULTY' ? 'badge-blue' : 'badge-purple'}`}>{m.memberType}</span></td>
                  <td>{m.email}</td>
                  <td>{m.phone}</td>
                  <td><span className={`badge ${m.active ? 'badge-green' : 'badge-red'}`}>{m.active ? 'Active' : 'Inactive'}</span></td>
                  <td>
                    {m.active && (
                      <button className="btn btn-danger btn-sm" onClick={() => handleDeactivate(m.id)}>Deactivate</button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
