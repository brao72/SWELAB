import { useState, useEffect } from 'react'
import { borrow } from '../api'

export default function Notifications({ memberId }) {
  const [notes, setNotes] = useState([])
  const [open, setOpen] = useState(false)

  useEffect(() => {
    borrow.notifications(memberId).then(setNotes).catch(() => {})
    const interval = setInterval(() => {
      borrow.notifications(memberId).then(setNotes).catch(() => {})
    }, 10000)
    return () => clearInterval(interval)
  }, [memberId])

  return (
    <div className="notif-wrapper">
      <button className="notif-bell" onClick={() => setOpen(!open)}>
        {notes.length > 0 && <span className="notif-badge">{notes.length}</span>}
        <span>Notifications</span>
      </button>
      {open && (
        <div className="notif-dropdown">
          {notes.length === 0 ? (
            <div className="notif-empty">No notifications</div>
          ) : (
            notes.map(n => (
              <div key={n.reservationId} className="notif-item">
                <strong>{n.bookTitle}</strong> is now available!
              </div>
            ))
          )}
        </div>
      )}
    </div>
  )
}
