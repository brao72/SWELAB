import { useState } from 'react'
import { auth } from '../api'

export default function Login({ onLogin }) {
  const [mode, setMode] = useState('librarian')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [memberId, setMemberId] = useState('')
  const [memberPassword, setMemberPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      let session
      if (mode === 'librarian') {
        session = await auth.loginLibrarian(username, password)
      } else {
        session = await auth.loginMember(memberId, memberPassword)
      }
      onLogin(session)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>LibraTrack</h1>
        <p className="login-subtitle">Uni Library Management System</p>

        <div className="tab-group">
          <button
            className={`tab ${mode === 'librarian' ? 'active' : ''}`}
            onClick={() => setMode('librarian')}
          >
            Librarian
          </button>
          <button
            className={`tab ${mode === 'member' ? 'active' : ''}`}
            onClick={() => setMode('member')}
          >
            Member
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          {mode === 'librarian' ? (
            <>
              <div className="form-group">
                <label>Username</label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Enter username"
                  required
                />
              </div>
              <div className="form-group">
                <label>Password</label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter password"
                  required
                />
              </div>
            </>
          ) : (
            <>
              <div className="form-group">
                <label>Member ID</label>
                <input
                  type="number"
                  value={memberId}
                  onChange={(e) => setMemberId(e.target.value)}
                  placeholder="Enter your member ID"
                  required
                />
              </div>
              <div className="form-group">
                <label>Password</label>
                <input
                  type="password"
                  value={memberPassword}
                  onChange={(e) => setMemberPassword(e.target.value)}
                  placeholder="Enter your password"
                  required
                />
              </div>
            </>
          )}

          {error && <div className="error-msg">{error}</div>}

          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>
      </div>
    </div>
  )
}
