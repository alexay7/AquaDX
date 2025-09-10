package icu.samnyan.aqua.sega.chusan.model.userdata

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity(name = "ChusanUserTeamPoints")
@Table(name = "chusan_user_team_points", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "team_id", "month_date"])])
class UserTeamPoints : Chu3UserEntity() {
    var teamId: Long = 0
    var monthlyPoints: Long = 0
    var monthDate: LocalDate? = null
}
