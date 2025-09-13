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
    val userTeamRepo: Chu3UserTeamRepo,
    val userTeamPointsRepo: Chu3UserTeamPointsRepo,
    val userInviteRepo: Chu3UserTeamInviteRepo,
    val teamRepo: Chu3TeamRepo
){
    @PostMapping("team-info")
    suspend fun teamInfo(@RP token:String) = us.jwt.auth(token){
        val foundTeam = userTeamRepo.findSingleByUser(it)() ?: (404 - "No team found")
        val currentPeriod = LocalDate.now().withDayOfMonth(1)

        val teamInfo = userTeamRepo.findTeamWithMembersAndPoints(foundTeam.team.id, currentPeriod)

        mapOf(
            "team" to foundTeam.team,
            "members" to teamInfo
        )
    }

    @PostMapping("team-ranking")
    suspend fun teamRanking(): List<RankingTeam> {
        val currentPeriod = LocalDate.now().withDayOfMonth(1)

        return userTeamPointsRepo.findTeamRanking(currentPeriod)
    }

    @PostMapping("create-team")
    suspend fun createTeam(@RP token:String, @RP name: String) = us.jwt.auth(token){
        val foundTeam = userTeamRepo.findSingleByUser(it)()

        if (foundTeam != null) (400 - "You are already on a team")

        teamRepo.save(Team().apply {
            owner = it
            teamName = name
        })

        val newTeam = teamRepo.findSingleByOwner(it)()!!

        userTeamRepo.save(UserTeam().apply {
            user = it
            team = newTeam
        })

        SUCCESS
    }

    @PostMapping("delete-team")
    suspend fun deleteTeam(@RP token:String) = us.jwt.auth(token){
        val foundTeam = teamRepo.findSingleByOwner(it)() ?: (404 - "No team found")

        teamRepo.delete(foundTeam)

        SUCCESS
    }

    @PostMapping("kick-user")
    suspend fun kickUser(@RP token:String, @RP username: String) = us.jwt.auth(token){
        val foundTeam = teamRepo.findSingleByOwner(it)() ?: (404 - "No team found")

        val candidateUser = us.cardByName(username) { it -> it.aquaUser } ?: (400 - "Candidate user not found")

        if (candidateUser.auId == it.auId) (400 - "You cannot kick yourself from the team")

        val candidateUserTeam = userTeamRepo.findSingleByUser(candidateUser)() ?: (400 - "That user is not on a team")

        if (candidateUserTeam.team.id != foundTeam.id) (400 - "That user is not on your team")

        userTeamRepo.delete(candidateUserTeam)

        SUCCESS
    }

    @PostMapping("give-owner")
    suspend fun giveOwner(@RP token:String, @RP username: String) = us.jwt.auth(token){
        val foundTeam = teamRepo.findSingleByOwner(it)() ?: (404 - "No team found")
        val candidateUser = us.cardByName(username) { it -> it.aquaUser } ?: (400 - "Candidate user not found")
        val candidateUserTeam = userTeamRepo.findSingleByUser(candidateUser)() ?: (400 - "That user is not on a team")

        if (candidateUserTeam.team.id != foundTeam.id) (400 - "That user is not on your team")

        if (candidateUser.auId == it.auId) (400 - "You cannot give ownership to yourself")

        teamRepo.findById(foundTeam.id).apply {
            if (this.isPresent){
                this.get().owner = candidateUser
                teamRepo.save(this.get())
            } else {
                (404 - "Team not found")
            }
        }

        SUCCESS
    }

    @PostMapping("change-team-name")
    suspend fun changeTeamName(@RP token:String, @RP name: String) = us.jwt.auth(token){
        val foundTeam = teamRepo.findSingleByOwner(it)() ?: (404 - "No team found")

        teamRepo.findById(foundTeam.id).apply {
            if (this.isPresent){
                this.get().teamName = name
                teamRepo.save(this.get())
            } else {
                (404 - "Team not found")
            }
        }

        SUCCESS
    }

    @PostMapping("leave-team")
    suspend fun leaveTeam(@RP token:String, @RP username: String?) = us.jwt.auth(token){
        val foundTeam = userTeamRepo.findSingleByUser(it)() ?: (404 - "No team found")

        if (foundTeam.team.owner.auId != it.auId){
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

                        val foundUser = candidateUser.aquaUser ?: (400 - "Candidate user not found")

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

    @PostMapping("get-invites")
    suspend fun getInvites(@RP token:String) = us.jwt.auth(token){
        userInviteRepo.findByUser(it).map { it ->
            mapOf(
                "inviteId" to it.id,
                "team" to it.team
            )
        }
    }

    @PostMapping("invite-user")
    suspend fun inviteUser(@RP token:String, @RP username: String) = us.jwt.auth(token){
        val candidateUserTeam = userTeamRepo.findSingleByUser(it)()

        if (candidateUserTeam != null) (400 - "That user is already on a team")

        val foundTeam = teamRepo.findSingleByOwner(it)() ?: (404 - "No team found")
        val candidateUser = us.cardByName(username) { it -> it.aquaUser } ?: (400 - "Candidate user not found")
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
        val invite = userInviteRepo.findById(inviteId)() ?: (404 - "Invite not found")
        val existingTeam = userTeamRepo.findSingleByUser(it)()

        if (invite.user.auId != it.auId) (400 - "That invite is not for you")

        if (existingTeam != null) (400 - "You are already on a team")

        val team = teamRepo.findById(invite.team.id)() ?: (404 - "Team not found")

        userTeamRepo.save(UserTeam().apply {
            user = it
            this.team = team
        })

        userInviteRepo.delete(invite)

        SUCCESS
    }

    @PostMapping("decline-invite")
    fun declineInvite(@RP token:String, @RP inviteId: Long) = us.jwt.auth(token){
        val invite = userInviteRepo.findById(inviteId)() ?: (404 - "Invite not found")
        val existingTeam = userTeamRepo.findSingleByUser(it)()

        // The owner of the team can also decline invites on behalf of the user
        if (invite.user.auId != it.auId && existingTeam?.team?.owner!=it) (400 - "That invite is not for you")

        userInviteRepo.delete(invite)

        SUCCESS
    }
}