package ru.underfish.eventservice.database.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.underfish.eventservice.database.entities.enums.AttendanceStatus
import java.util.UUID

@Entity
@Table(name = "event_attendances")
class EventAttendance(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    var event: Event,
    @Column(name = "user_id", nullable = false)
    var userId: UUID,
) : AbstractEntity() {
    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 20)
    var status: AttendanceStatus = AttendanceStatus.PENDING
}
