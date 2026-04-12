import { useState, useEffect } from 'react'
import { fines } from '../api'

export default function Fines({ session }) {
  const [memberId, setMemberId] = useState(session.role === 'MEMBER' ? session.userId : '')
  const [fineList, setFineList] = useState([])
  const [total, setTotal] = useState(0)
  const [loaded, setLoaded] = useState(false)
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  const isMember = session.role === 'MEMBER'

  useEffect(() => {
    if (isMember) loadFines(session.userId)
  }, [])

  async function loadFines(id) {
    setError('')
    setMsg('')
    try {
      const [unpaid, totalRes] = await Promise.all([
        fines.getUnpaid(id),
        fines.getTotal(id),
      ])
      setFineList(unpaid)
      setTotal(totalRes.total)
      setLoaded(true)
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleSearch(e) {
    e.preventDefault()
    loadFines(memberId)
  }

  async function handlePay(fineId) {
    setError('')
    setMsg('')
    try {
      await fines.pay(fineId, memberId)
      setMsg(`Fine #${fineId} paid successfully!`)
      loadFines(memberId)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page">
      <h2>Fines</h2>

      {error && <div className="error-msg">{error}</div>}
      {msg && <div className="success-msg">{msg}</div>}

      {!isMember && (
        <form className="search-bar" onSubmit={handleSearch}>
          <input
            type="number"
            placeholder="Enter Member ID"
            value={memberId}
            onChange={e => setMemberId(e.target.value)}
            required
          />
          <button type="submit" className="btn btn-secondary">View Fines</button>
        </form>
      )}

      {loaded && (
        <>
          <div className="card" style={{ marginBottom: '1.5rem' }}>
            <div className="stat-row">
              <div className="stat">
                <span className="stat-label">Unpaid Fines</span>
                <span className="stat-value">{fineList.length}</span>
              </div>
              <div className="stat">
                <span className="stat-label">Total Outstanding</span>
                <span className="stat-value stat-danger">Rs.{total.toFixed(2)}</span>
              </div>
            </div>
          </div>

          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Fine ID</th>
                  <th>Borrow Record</th>
                  <th>Amount</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {fineList.length === 0 ? (
                  <tr><td colSpan="5" className="empty">No unpaid fines</td></tr>
                ) : (
                  fineList.map(f => (
                    <tr key={f.id}>
                      <td>{f.id}</td>
                      <td>{f.borrowRecordId}</td>
                      <td>Rs.{f.amount.toFixed(2)}</td>
                      <td>{f.createdAt ? new Date(f.createdAt).toLocaleDateString() : '-'}</td>
                      <td>
                        <button className="btn btn-primary btn-sm" onClick={() => handlePay(f.id)}>
                          Pay
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  )
}
