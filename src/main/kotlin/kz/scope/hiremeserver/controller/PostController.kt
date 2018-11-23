package kz.scope.hiremeserver.controller

import kz.scope.hiremeserver.exception.ResourceNotFoundException
import kz.scope.hiremeserver.model.Company
import kz.scope.hiremeserver.model.Post
import kz.scope.hiremeserver.model.User
import kz.scope.hiremeserver.payload.*
import kz.scope.hiremeserver.repository.CompanyRepository
import kz.scope.hiremeserver.repository.JobOfferRepository
import kz.scope.hiremeserver.repository.PostRepository
import kz.scope.hiremeserver.repository.UserRepository
import kz.scope.hiremeserver.security.CurrentUser
import kz.scope.hiremeserver.security.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.validation.Valid

private val logger1 = LoggerFactory.getLogger(PostController::class.java)

@RestController
@RequestMapping("/api")
class PostController {

    @Autowired
    lateinit var postRepository: PostRepository

    @Autowired
    lateinit var companyRepository: CompanyRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var jobOfferRepository: JobOfferRepository

//    @GetMapping("/posts/{id}")
//    @PreAuthorize("hasRole('USER')")
//    fun getPost(@PathVariable(value = "id") id: Long): PostResponse {
//        val postOptional = postRepository.findById(id)
//        val post: Post
//
//        if (postOptional.isPresent) {
//            post = postOptional.get()
//        } else {
//            throw ResourceNotFoundException("Post", "id", id)
//        }
//
//        val jobOfferResponse: JobOfferResponse?
//        val jobOffer = post.jobOffer
//
//        if (jobOffer == null) {
//            // this post is just job offer
//            jobOfferResponse = null
//        } else {
//            // this post is not a job offer, but rather a simple yet powerful post
//            val company = CompanyJobOfferResponse(jobOffer.company.id, jobOffer.company.name, jobOffer.company.logo)
//            val locations: MutableList<String> = ArrayList<String>()
//            for (location in jobOffer.locations) {
//                locations.add(location.location)
//            }
//
//            jobOfferResponse = JobOfferResponse(jobOffer.id, company, jobOffer.position,
//                    jobOffer.responsibilities, jobOffer.qualifications, locations, jobOffer.createdAt, jobOffer.updatedAt)
//        }
//
//        val author: Author
//        if (post.isCompany) {
//            // author of the post is company
//            val companyOptional = companyRepository.findById(post.authorId)
//            val company: Company
//
//            if (companyOptional.isPresent) {
//                company = companyOptional.get()
//            } else {
//                throw ResourceNotFoundException("Company", "id", post.authorId)
//            }
//
//            author = Author(company.id, company.name)
//        } else {
//            val userOptional = userRepository.findById(post.authorId)
//            val user: User
//
//            if (userOptional.isPresent) {
//                user = userOptional.get()
//            } else {
//                throw ResourceNotFoundException("User", "id", post.authorId)
//            }
//
//            author = Author(user.id, user.fullname)
//        }
//        return PostResponse(post.id, post.isCompany, author, post.title, post.text, post.photoLink, jobOfferResponse, post.createdAt)
//    }
//
//    @PostMapping("post")
//    @PreAuthorize("hasRole('USER')")
//    fun addPost(@CurrentUser currentUser: UserPrincipal, @Valid @RequestBody postRequest: PostRequest)
//            : ResponseEntity<*> {
//        if (postRequest.company) {
//            // author of the post is company
//            val companyOptional = companyRepository.findById(postRequest.author)
//
//            if (companyOptional.isPresent.not()) {
//                return ResponseEntity(ApiResponse(false, "Such company does not exists"), HttpStatus.EXPECTATION_FAILED)
//            }
//
//            // getting current user of class User
//            val current_user_id = currentUser.id
//            val current_user_optional = userRepository.findById(current_user_id)
//            val current_user: User
//
//            if (current_user_optional.isPresent) {
//                current_user = current_user_optional.get()
//            } else {
//                throw ResourceNotFoundException("User", "id", current_user_id)
//            }
//
//            val jobOffer: JobOffer = JobOfferController.createJobOffer
//
//
//            val post = Post(postRequest.company, postRequest.author, postRequest.title, postRequest.text, postRequest.photo_link)
//
//
//            val result = postRepository.save(post)
//            val location = ServletUriComponentsBuilder
//                    .fromCurrentContextPath().path("/posts/{id}")
//                    .buildAndExpand(result.id).toUri()
//
//            return ResponseEntity.created(location).body(ApiResponse(true, "Post created successfully"))
//
//        } else {
//            // author of the post is user
//            val userOptional = userRepository.findById(postRequest.author)
//
//            if (userOptional.isPresent.not()) {
//                return ResponseEntity(ApiResponse(false, "Such user does not exists"), HttpStatus.EXPECTATION_FAILED)
//            } else if (userOptional.get().id != currentUser.id) {
//                return ResponseEntity(ApiResponse(false, "You can only create post on your account."), HttpStatus.UNAUTHORIZED)
//            }
//
//            val post = Post(postRequest.company, postRequest.author, postRequest.title, postRequest.text, postRequest.photo_link)
//
//            for (jobOfferId in postRequest.jobOffersIds) {
//                val jobOfferOptional = jobOfferRepository.findById(jobOfferId)
//                if(jobOfferOptional.isPresent) post.jobOffers.add(jobOfferOptional.get())
//                else return ResponseEntity(ApiResponse(false, "Job Offer with one of ids does not exists")
//                        , HttpStatus.EXPECTATION_FAILED)
//            }
//
//            val result = postRepository.save(post)
//            val location = ServletUriComponentsBuilder
//                    .fromCurrentContextPath().path("/posts/{id}")
//                    .buildAndExpand(result.id).toUri()
//
//            return ResponseEntity.created(location).body(ApiResponse(true, "Post created successfully"))
//        }
//    }
//
//    @GetMapping("/posts")
//    @PreAuthorize("hasRole('USER')")
//    fun getPosts(): List<PostResponse> {
//        val topTen: PageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt")
//        val posts = postRepository.findAll(topTen)
//
//        val responses: MutableList<PostResponse> = ArrayList<PostResponse>()
//
//        for (post in posts) {
//            val jobOffer = post.jobOffer
//            val jobOfferResponse: JobOfferResponse?
//
//            if (jobOffer == null) {
//                 jobOfferResponse = null
//            } else {
//                val company = CompanyJobOfferResponse(jobOffer.company.id, jobOffer.company.name, jobOffer.company.logo)
//                val locations: MutableList<String> = ArrayList<String>()
//                for (location in jobOffer.locations) {
//                    locations.add(location.location)
//                }
//                jobOfferResponse = JobOfferResponse(jobOffer.id, company, jobOffer.position,
//                            jobOffer.responsibilities, jobOffer.qualifications, locations, jobOffer.createdAt, jobOffer.updatedAt)
//            }
//
//            val author: Author
//            if (post.isCompany) {
//                // author of the post is company
//                val companyOptional = companyRepository.findById(post.authorId)
//                val company: Company
//
//                if (companyOptional.isPresent) {
//                    company = companyOptional.get()
//                } else {
//                    throw ResourceNotFoundException("Company", "id", post.authorId)
//                }
//
//                author = Author(company.id, company.name)
//            } else {
//                val userOptional = userRepository.findById(post.authorId)
//                val user: User
//
//                if (userOptional.isPresent) {
//                    user = userOptional.get()
//                } else {
//                    throw ResourceNotFoundException("User", "id", post.authorId)
//                }
//
//                author = Author(user.id, user.fullname)
//            }
//
//            responses.add(PostResponse(post.id, post.isCompany, author, post.title, post.text, post.photoLink, jobOfferResponse, post.createdAt))
//
//        }
//        return responses
//    }
//
//    @GetMapping("/posts-following")
//    @PreAuthorize("hasRole('USER')")
//    fun getPostsFollowing(@CurrentUser currentUser: UserPrincipal): List<PostResponse> {
//
//        // getting current user of class User
//        val currentUserId = currentUser.id
//        val currentUserOptional = userRepository.findById(currentUserId)
//        val curUser: User
//
//        if (currentUserOptional.isPresent) {
//            curUser = currentUserOptional.get()
//        } else {
//            throw ResourceNotFoundException("User", "id", currentUserId)
//        }
//
//        val relevantPosts: MutableList<Post> = ArrayList()
//        val responses: MutableList<PostResponse> = ArrayList()
//
////        val topTen: PageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt")
////        val posts = postRepository.findAll(topTen)
//
//        // getting following entities
//        val followingUsers = curUser.following
//        val followingCompanies = curUser.followingCompanies
//
//        for (user in followingUsers) {
//            val tempList = postRepository.findByIsCompanyInAndAuthorIdIn(
//                    isCompany = false,
//                    authorId = user.id
//            )
//            relevantPosts.addAll(tempList)
//        }
//
//        for (company in followingCompanies) {
//            val tempList = postRepository.findByIsCompanyInAndAuthorIdIn(
//                    isCompany = true,
//                    authorId = company.id
//            )
//            relevantPosts.addAll(tempList)
//        }
//
//        relevantPosts.sort()
//
//        val posts: MutableList<Post> = ArrayList()
//
//        for (i in 0..9 ) {
//            if (i == relevantPosts.size) {
//                break
//            }
//            posts.add(relevantPosts[i])
//        }
//
//        for (post in posts) {
//            val jobOffer = post.jobOffer
//            val jobOfferResponse: JobOfferResponse?
//
//            if (jobOffer == null) {
//                jobOfferResponse = null
//            } else {
//                val company = CompanyJobOfferResponse(jobOffer.company.id, jobOffer.company.name, jobOffer.company.logo)
//                val locations: MutableList<String> = ArrayList<String>()
//                for (location in jobOffer.locations) {
//                    locations.add(location.location)
//                }
//                jobOfferResponse = JobOfferResponse(jobOffer.id, company, jobOffer.position,
//                        jobOffer.responsibilities, jobOffer.qualifications, locations, jobOffer.createdAt, jobOffer.updatedAt)
//            }
//
//            val author: Author
//            if (post.isCompany) {
//                // author of the post is company
//                val companyOptional = companyRepository.findById(post.authorId)
//                val company: Company
//
//                if (companyOptional.isPresent) {
//                    company = companyOptional.get()
//                } else {
//                    throw ResourceNotFoundException("Company", "id", post.authorId)
//                }
//
//                author = Author(company.id, company.name)
//            } else {
//                val userOptional = userRepository.findById(post.authorId)
//                val user: User
//
//                if (userOptional.isPresent) {
//                    user = userOptional.get()
//                } else {
//                    throw ResourceNotFoundException("User", "id", post.authorId)
//                }
//
//                author = Author(user.id, user.fullname)
//            }
//
//            responses.add(PostResponse(post.id, post.isCompany, author, post.title, post.text, post.photoLink, jobOfferResponse, post.createdAt))
//
//        }
//        return responses
//    }
//
//    @GetMapping("/my-posts")
//    @PreAuthorize("hasRole('USER')")
//    fun getMyPosts(@CurrentUser currentUser: UserPrincipal): List<PostResponse> {
//        val responses: MutableList<PostResponse> = ArrayList()
//
//        val posts = postRepository.findByIsCompanyInAndAuthorIdIn(
//                isCompany = false,
//                authorId = currentUser.id
//        )
//
//        for (post in posts) {
//            val jobOffer = post.jobOffer
//            val jobOfferResponse: JobOfferResponse?
//
//            if (jobOffer == null) {
//                jobOfferResponse = null
//            } else {
//                val company = CompanyJobOfferResponse(jobOffer.company.id, jobOffer.company.name, jobOffer.company.logo)
//                val locations: MutableList<String> = ArrayList<String>()
//                for (location in jobOffer.locations) {
//                    locations.add(location.location)
//                }
//                jobOfferResponse = JobOfferResponse(jobOffer.id, company, jobOffer.position,
//                        jobOffer.responsibilities, jobOffer.qualifications, locations, jobOffer.createdAt, jobOffer.updatedAt)
//            }
//
//            val author: Author
//            if (post.isCompany) {
//                // author of the post is company
//                val companyOptional = companyRepository.findById(post.authorId)
//                val company: Company
//
//                if (companyOptional.isPresent) {
//                    company = companyOptional.get()
//                } else {
//                    throw ResourceNotFoundException("Company", "id", post.authorId)
//                }
//
//                author = Author(company.id, company.name)
//            } else {
//                val userOptional = userRepository.findById(post.authorId)
//                val user: User
//
//                if (userOptional.isPresent) {
//                    user = userOptional.get()
//                } else {
//                    throw ResourceNotFoundException("User", "id", post.authorId)
//                }
//
//                author = Author(user.id, user.fullname)
//            }
//
//            responses.add(PostResponse(post.id, post.isCompany, author, post.title, post.text, post.photoLink, jobOfferResponse, post.createdAt))
//
//        }
//        return responses
//    }
}