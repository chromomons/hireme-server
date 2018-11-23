package kz.scope.hiremeserver.controller

import kz.scope.hiremeserver.exception.ResourceNotFoundException
import kz.scope.hiremeserver.model.Company
import kz.scope.hiremeserver.model.User
import kz.scope.hiremeserver.payload.*
import kz.scope.hiremeserver.repository.CompanyRepository
import kz.scope.hiremeserver.repository.UserRepository
import kz.scope.hiremeserver.security.CurrentUser
import kz.scope.hiremeserver.security.UserPrincipal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api")
class FollowingController {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var companyRepository: CompanyRepository

    @GetMapping("/{username}/following")
    @PreAuthorize("hasRole('USER')")
    fun getFollowingUsers(@PathVariable(value = "username") username: String): List<UserSummary> {
        // working with the target user
        val targetUser = userRepository.findByUsername(username)

        if (targetUser != null) {
            // the target user exists
            val followingList: MutableList<UserSummary> = ArrayList()
            for (followingUser in targetUser.following) {
                followingList.add(UserSummary(
                        id = followingUser.id,
                        username = followingUser.username,
                        fullname = followingUser.fullname
                ))
            }
            return followingList
        } else throw ResourceNotFoundException("User", "username", username)
    }

    @GetMapping("/{username}/following-companies")
    @PreAuthorize("hasRole('USER')")
    fun getFollowingCompanies(@PathVariable(value = "username") username: String): List<CompanySummary> {
        // working with the target user
        val targetUser = userRepository.findByUsername(username)

        if (targetUser != null) {
            // the target user exists
            val followingList: MutableList<CompanySummary> = ArrayList()
            for (company in targetUser.followingCompanies) {
                followingList.add(CompanySummary(
                        id = company.id,
                        name = company.name,
                        description = company.description
                ))
            }
            return followingList
        } else throw ResourceNotFoundException("User", "username", username)
    }

    @GetMapping("/{username}/followers")
    @PreAuthorize("hasRole('USER')")
    fun getFollowedUsers(@PathVariable(value = "username") username: String): List<UserSummary> {
        val targetUser = userRepository.findByUsername(username)

        if (targetUser != null) {
            // the target user exists
            val followingList: MutableList<UserSummary> = ArrayList()
            for (followingUser in userRepository.findByFollowing(targetUser)) {
                followingList.add(UserSummary(
                        id = followingUser.id,
                        username = followingUser.username,
                        fullname = followingUser.fullname
                ))
            }
            return followingList
        } else throw ResourceNotFoundException("User", "username", username)
    }

    @GetMapping("/{company_id}/company-followers")
    @PreAuthorize("hasRole('USER')")
    fun getCompanyFollowers(@PathVariable(value = "company_id") company_id: Long): List<UserSummary> {
        val targetCompanyOptional = companyRepository.findById(company_id)
        val targetCompany: Company

        if (targetCompanyOptional.isPresent) {
            // the target company exists
            targetCompany = targetCompanyOptional.get()
            val followerList: MutableList<UserSummary> = ArrayList()
            for (followingUser in targetCompany.followers) {
                followerList.add(UserSummary(
                        id = followingUser.id,
                        username = followingUser.username,
                        fullname = followingUser.fullname
                ))
            }
            return followerList
        } else throw ResourceNotFoundException("Company", "company_id", company_id)
    }

    @PostMapping("/{username}/follow")
    @PreAuthorize("hasRole('USER')")
    fun follow(@CurrentUser currentUser: UserPrincipal,
               @PathVariable(value = "username") username: String): ResponseEntity<*> {
        // getting current user of class User
        val currentUserId = currentUser.id
        val currentUserOptional = userRepository.findById(currentUserId)
        val currUser: User

        if (currentUserOptional.isPresent) {
            currUser = currentUserOptional.get()
        } else {
            throw ResourceNotFoundException("User", "id", currentUserId)
        }

        // working with the target user
        val targetUser = userRepository.findByUsername(username)

        if (targetUser != null) {
            if (targetUser.id == currUser.id) {
                return ResponseEntity.badRequest().body("You cannot follow yourself")
            }
            if (!currUser.following.add(targetUser)) {
                // already following
                return ResponseEntity.badRequest().body("You already follow this user")
            }

            val result = userRepository.save(currUser)

            val location = ServletUriComponentsBuilder
                    .fromCurrentContextPath().path("/users/{username}")
                    .buildAndExpand(result.username).toUri()

            return ResponseEntity.created(location).body(ApiResponse(true, "You are now following this user"))
        } else throw ResourceNotFoundException("User", "username", username)
    }

    @DeleteMapping("/{username}/unfollow")
    @PreAuthorize("hasRole('USER')")
    fun unfollow(@CurrentUser currentUser: UserPrincipal,
                 @PathVariable(value = "username") username: String): ResponseEntity<*> {
        // getting current user of class User
        val currentUserId = currentUser.id
        val currentUserOptional = userRepository.findById(currentUserId)
        val currUser: User

        if (currentUserOptional.isPresent) {
            currUser = currentUserOptional.get()
        } else {
            throw ResourceNotFoundException("User", "id", currentUserId)
        }

        // working with the target user
        val targetUser = userRepository.findByUsername(username)

        if (targetUser != null) {
            if (targetUser.id == currUser.id) {
                return ResponseEntity.badRequest().body("You cannot unfollow yourself")
            }
            if (!currUser.following.remove(targetUser)) {
                // already following
                return ResponseEntity.badRequest().body("You do not follow this user")
            }

            val result = userRepository.save(currUser)

            val location = ServletUriComponentsBuilder
                    .fromCurrentContextPath().path("/users/{username}")
                    .buildAndExpand(result.username).toUri()

            return ResponseEntity.created(location).body(ApiResponse(true, "You are not following this user anymore"))
        } else throw ResourceNotFoundException("User", "username", username)
    }

    @PostMapping("/{company_id}/follow-company")
    @PreAuthorize("hasRole('USER')")
    fun followCompany(@CurrentUser currentUser: UserPrincipal,
                      @PathVariable(value = "company_id") company_id: Long): ResponseEntity<*> {
        // getting current user of class User
        val currentUserId = currentUser.id
        val currentUserOptional = userRepository.findById(currentUserId)
        val currUser: User

        if (currentUserOptional.isPresent) {
            currUser = currentUserOptional.get()
        } else {
            throw ResourceNotFoundException("User", "id", currentUserId)
        }

        // working with the target company
        val targetCompanyOptional = companyRepository.findById(company_id)
        val targetCompany: Company

        if (targetCompanyOptional.isPresent) {
            targetCompany = targetCompanyOptional.get()
            if (!currUser.followingCompanies.add(targetCompany)) {
                // already following
                return ResponseEntity.badRequest().body("You already follow this company")
            }
            val result = userRepository.save(currUser)
            val location = ServletUriComponentsBuilder
                    .fromCurrentContextPath().path("/users/{username}")
                    .buildAndExpand(result.username).toUri()

            return ResponseEntity.created(location).body(ApiResponse(true, "You are now following this company"))

        } else throw ResourceNotFoundException("Company", "company_id", company_id)
    }

    @DeleteMapping("/{company_id}/unfollow-company")
    @PreAuthorize("hasRole('USER')")
    fun unfollowCompany(@CurrentUser currentUser: UserPrincipal,
                        @PathVariable(value = "company_id") company_id: Long): ResponseEntity<*> {
        // getting current user of class User
        val currentUserId = currentUser.id
        val currentUserOptional = userRepository.findById(currentUserId)
        val currUser: User

        if (currentUserOptional.isPresent) {
            currUser = currentUserOptional.get()
        } else {
            throw ResourceNotFoundException("User", "id", currentUserId)
        }

        // working with the target company
        val targetCompanyOptional = companyRepository.findById(company_id)
        val targetCompany: Company

        if (targetCompanyOptional.isPresent) {
            targetCompany = targetCompanyOptional.get()
            if (!currUser.followingCompanies.remove(targetCompany)) {
                // already following
                return ResponseEntity.badRequest().body("You do not follow this company")
            }
            val result = userRepository.save(currUser)
            val location = ServletUriComponentsBuilder
                    .fromCurrentContextPath().path("/users/{username}")
                    .buildAndExpand(result.username).toUri()

            return ResponseEntity.created(location).body(ApiResponse(true, "You are not following this company anymore"))

        } else throw ResourceNotFoundException("Company", "company_id", company_id)
    }

    @GetMapping("/{company_id}/is-following")
    @PreAuthorize("hasRole('USER')")
    fun isFollowingCompany(@CurrentUser currentUser: UserPrincipal,
                           @PathVariable(value = "company_id") company_id: Long): Boolean {
        // getting current user of class User
        val currentUserId = currentUser.id
        val currentUserOptional = userRepository.findById(currentUserId)
        val currUser: User

        if (currentUserOptional.isPresent) {
            currUser = currentUserOptional.get()
        } else {
            throw ResourceNotFoundException("User", "id", currentUserId)
        }

        // working with the target company
        val targetCompanyOptional = companyRepository.findById(company_id)
        val targetCompany: Company

        if (targetCompanyOptional.isPresent) {
            targetCompany = targetCompanyOptional.get()

            return currUser.followingCompanies.contains(targetCompany)

        } else throw ResourceNotFoundException("Company", "company_id", company_id)
    }

    @GetMapping("/{username}/is-following")
    @PreAuthorize("hasRole('USER')")
    fun isFollowing(@CurrentUser currentUser: UserPrincipal,
                    @PathVariable(value = "username") username: String): Boolean {
        // getting current user of class User
        val currentUserId = currentUser.id
        val currentUserOptional = userRepository.findById(currentUserId)
        val currUser: User

        if (currentUserOptional.isPresent) {
            currUser = currentUserOptional.get()
        } else {
            throw ResourceNotFoundException("User", "id", currentUserId)
        }

        // working with the target user
        val targetUser = userRepository.findByUsername(username)

        if (targetUser != null) {
            if (targetUser.id == currUser.id) {
                return false
            }
            return currUser.following.contains(targetUser)
        } else throw ResourceNotFoundException("User", "username", username)
    }
}