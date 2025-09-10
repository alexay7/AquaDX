package icu.samnyan.aqua.sega.chusan.model.userdata

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
class UserTeamInvite : Chu3UserEntity() {
    @ManyToOne
    @JoinColumn(name = "team", referencedColumnName = "id")
    @Fetch(FetchMode.JOIN)
    var team: Team = Team()
}