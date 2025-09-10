package icu.samnyan.aqua.net.games.chu3

import ext.API
import ext.RP
import ext.invoke
import ext.minus
import icu.samnyan.aqua.net.db.AquaUserServices
import icu.samnyan.aqua.net.utils.SUCCESS
import icu.samnyan.aqua.sega.chusan.model.*
import icu.samnyan.aqua.sega.chusan.model.userdata.UserTeam
import icu.samnyan.aqua.sega.chusan.model.userdata.UserTeamInvite
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import java.time.LocalDate

@RestController
@API("api/v2/game/chu3")
class Chu3Teams(
    val us: AquaUserServices,
    val userDataRepo: Chu3UserDataRepo,
    val userTeamRepo: Chu3UserTeamRepo,
    val userTeamPointsRepo: Chu3UserTeamPointsRepo,
    val userInviteRepo: Chu3UserTeamInviteRepo,
    val teamRepo: Chu3TeamRepo
){
    @PostMapping("team-ranking")
    suspend fun teamRanking(): List<RankingTeam> {
        val currentPeriod = LocalDate.now().withDayOfMonth(1)

        return userTeamPointsRepo.findTeamRanking(currentPeriod)
    }

    @PostMapping("leave-team")
    suspend fun leaveTeam(@RP token:String, @RP username: String?) = us.jwt.auth(token){
        val u = userDataRepo.findByCard(it.ghostCard) ?: (404 - "Game data not found")

        val foundTeam = userTeamRepo.findSingleByUser(u)() ?: (404 - "No team found")

        if (foundTeam.team.owner?.id != u.id){
            // If the user is not the owner then it can leave the team normally
            userTeamRepo.delete(foundTeam)
        } else {
            // If the user is the owner and is the only member of the team, then the team will be deleted
            val members = userTeamRepo.countTeamMembers(foundTeam.team.id)
            if (members == 1){
                teamRepo.delete(foundTeam.team)
            } else {
                // Turn the candidate user into the owner
                teamRepo.findById(foundTeam.team.id).apply {
                    if (username != null){
                        val candidateUser = us.cardByName(username)

                        val foundUser = userDataRepo.findByCard(candidateUser) ?: (400 - "Candidate user not found")

                        userTeamRepo.delete(foundTeam)
                        this.get().owner = foundUser
                        teamRepo.save(this.get())
                    } else {
                        // If no candidate is provided, delete the team
                        teamRepo.delete(this.get())
                        return@apply
                    }
                }
            }
        }

        SUCCESS
    }

    @PostMapping("invite-user")
    suspend fun inviteUser(@RP token:String, @RP username: String) = us.jwt.auth(token){
        val u = userDataRepo.findByCard(it.ghostCard) ?: (404 - "Game data not found")
        val foundTeam = teamRepo.findSingleByOwner(u)() ?: (404 - "No team found")

        val candidateCard = us.cardByName(username)
        val candidateUser = userDataRepo.findByCard(candidateCard) ?: (404 - "That user does not exist")
        val candidateUserTeam = userTeamRepo.findSingleByUser(candidateUser)()

        if (candidateUserTeam != null) (400 - "That user is already on a team")

        val candidateUserInvite = userInviteRepo.findSingleByUserAndTeamId(candidateUser, foundTeam.id)()

        if (candidateUserInvite != null) (400 - "That user has already been invited")

        val totalTeamInvites = userInviteRepo.findByTeamId(foundTeam.id).count()
        val totalTeamMembers = userTeamRepo.countTeamMembers(foundTeam.id)

        if (totalTeamMembers + totalTeamInvites >= 20) (400 - "The team is full")

        userInviteRepo.save(UserTeamInvite().apply {
            user = candidateUser
            team = foundTeam
        })

        SUCCESS
    }

    @PostMapping("accept-invite")
    fun acceptInvite(@RP token:String, @RP inviteId: Long) = us.jwt.auth(token){
        val u = userDataRepo.findByCard(it.ghostCard) ?: (404 - "Game data not found")
        val invite = userInviteRepo.findById(inviteId)() ?: (404 - "Invite not found")
        val existingTeam = userTeamRepo.findSingleByUser(u)()

        if (invite.user.id != u.id) (400 - "That invite is not for you")

        if (existingTeam != null) (400 - "You are already on a team")

        val team = teamRepo.findById(invite.team.id)() ?: (404 - "Team not found")

        userTeamRepo.save(UserTeam().apply {
            user = u
            this.team = team
        })

        userInviteRepo.delete(invite)

        SUCCESS
    }

    @PostMapping("decline-invite")
    fun declineInvite(@RP token:String, @RP inviteId: Long) = us.jwt.auth(token){
        val u = userDataRepo.findByCard(it.ghostCard) ?: (404 - "Game data not found")
        val invite = userInviteRepo.findById(inviteId)() ?: (404 - "Invite not found")
        val existingTeam = userTeamRepo.findSingleByUser(u)()

        // The owner of the team can also decline invites on behalf of the user
        if (invite.user.id != u.id && existingTeam?.team?.owner!=u) (400 - "That invite is not for you")

        userInviteRepo.delete(invite)

        SUCCESS
    }
}