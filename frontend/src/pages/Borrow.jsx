import { useState } from 'react'
import { borrow } from '../api'

export default function Borrow({ session }) {
  const [action, setAction] = useState('issue')
  const [memberId, setMemberId] = useState(session.role === 'MEMBER' ? session.userId : '')
  const [isbn, setIsbn] = useState('')
  const [historyId, setHistoryId] = useState(session.role === 'MEMBER' ? session.userId : '')
  const [history, setHistory] = useState(null)
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')

  const isMember = session.role === 'MEMBER'

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setResult(null)
    try {
      let res
      if (action === 'issue') {
        res = await borrow.issue(memberId, isbn)
        setResult({ type: 'success', text: `Book issued! Due date: ${res.dueDate}` })
      } else if (action === 'return') {
        res = await borrow.returnBook(memberId, isbn)
        const fineMsg = res.fineAmount > 0
          ? ` Overdue by ${res.daysOverdue} days. Fine: Rs.${res.fineAmount.toFixed(2)}`
          : ' No fines.'
        setResult({ type: 'success', text: `Book returned.${fineMsg}` })
      } else {
        res = await borrow.reserve(memberId, isbn)
        setResult({ type: 'success', text: `Reservation placed! ID: ${res.id}` })
      }
      setIsbn('')
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleHistory(e) {
    e.preventDefault()
    setError('')
    try {
      const data = await borrow.history(historyId)
      setHistory(data)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page">
      <h2>Borrow Management</h2>

      {error && <div className="error-msg">{error}</div>}
      {result && <div className={`success-msg`}>{result.text}</div>}

      <div className="card form-card">
        <div className="tab-group">
          {(session.role === 'LIBRARIAN') && (
            <button className={`tab ${action === 'issue' ? 'active' : ''}`} onClick={() => setAction('issue')}>
              Issue Book
            </button>
          )}
          {(session.role === 'LIBRARIAN') && (
            <button className={`tab ${action === 'return' ? 'active' : ''}`} onClick={() => setAction('return')}>
              Return Book
            </button>
          )}
          <button className={`tab ${action === 'reserve' ? 'active' : ''}`} onClick={() => setAction('reserve')}>
            Reserve Book
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            {!isMember && (
              <div className="form-group">
                <label>Member ID</label>
                <input
                  type="number"
                  value={memberId}
                  onChange={e => setMemberId(e.target.value)}
                  required
                />
              </div>
            )}
            <div className="form-group">
              <label>ISBN</label>
              <input
                value={isbn}
                onChange={e => setIsbn(e.target.value)}
                placeholder="Enter book ISBN"
                required
              />
            </div>
          </div>
          <button type="submit" className="btn btn-primary">
            {action === 'issue' ? 'Issue Book' : action === 'return' ? 'Return Book' : 'Reserve Book'}
          </button>
        </form>
      </div>

      <div className="card form-card" style={{ marginTop: '1.5rem' }}>
        <h3>Borrowing History</h3>
        <form onSubmit={handleHistory} className="form-row">
          {!isMember && (
            <div className="form-group">
              <label>Member ID</label>
              <input
                type="number"
                value={historyId}
                onChange={e => setHistoryId(e.target.value)}
                required
              />
            </div>
          )}
          <div className="form-group" style={{ alignSelf: 'flex-end' }}>
            <button type="submit" className="btn btn-secondary">Load History</button>
          </div>
        </form>

        {history && (
          <div className="table-container" style={{ marginTop: '1rem' }}>
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Book ID</th>
                  <th>Issued</th>
                  <th>Due</th>
                  <th>Returned</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {history.length === 0 ? (
                  <tr><td colSpan="6" className="empty">No records found</td></tr>
                ) : (
                  history.map(r => (
                    <tr key={r.id}>
                      <td>{r.id}</td>
                      <td>{r.bookId}</td>
                      <td>{r.issueDate}</td>
                      <td>{r.dueDate}</td>
                      <td>{r.returnDate || '-'}</td>
                      <td>
                        <span className={`badge ${r.returned ? 'badge-green' : 'badge-yellow'}`}>
                          {r.returned ? 'Returned' : 'Active'}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
