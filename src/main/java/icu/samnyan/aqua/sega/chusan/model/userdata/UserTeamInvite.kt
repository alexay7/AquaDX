package icu.samnyan.aqua.sega.chusan.model.userdata

import com.fasterxml.jackson.annotation.JsonIgnore
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

@Entity(name = "ChusanUserTeamInvite")
@Table(name = "chusan_user_team_invite", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "team"])])
class UserTeamInvite : BaseEntity() {
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: AquaNetUser = AquaNetUser()

    @ManyToOne
    @JoinColumn(name = "team", referencedColumnName = "id")
    @Fetch(FetchMode.JOIN)
    var team: Team = Team()
}