package icu.samnyan.aqua.sega.chusan.model.userdata

import icu.samnyan.aqua.net.db.AquaNetUser
import icu.samnyan.aqua.net.games.BaseEntity
import icu.samnyan.aqua.sega.chusan.model.Team
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.time.LocalDate

@Entity(name = "ChusanUserTeamPoints")
@Table(name = "chusan_user_team_points", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "team_id", "month_date"])])
class UserTeamPoints : BaseEntity() {
    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: AquaNetUser = AquaNetUser()

    @ManyToOne
    @JoinColumn(name = "team", referencedColumnName = "id")
    @Fetch(FetchMode.JOIN)
    var team: Team = Team()
    var monthlyPoints: Long = 0
    var monthDate: LocalDate? = null
}
