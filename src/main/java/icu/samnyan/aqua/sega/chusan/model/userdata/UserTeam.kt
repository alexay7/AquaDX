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

@Entity(name = "ChusanUserTeam")
@Table(name = "chusan_user_team", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id"])])
class UserTeam : BaseEntity() {
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    var user: AquaNetUser = AquaNetUser()

    @ManyToOne
    @JoinColumn(name = "team", referencedColumnName = "id")
    @Fetch(FetchMode.JOIN)
    @JsonIgnore
    var team: Team = Team()
}
