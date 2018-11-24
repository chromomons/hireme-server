package kz.scope.hiremeserver.controller

import com.google.gson.Gson
import kz.scope.hiremeserver.exception.ResourceNotFoundException
import kz.scope.hiremeserver.model.Company
import kz.scope.hiremeserver.model.EmployerInfo
import kz.scope.hiremeserver.model.Log
import kz.scope.hiremeserver.payload.*
import kz.scope.hiremeserver.repository.CompanyRepository
import kz.scope.hiremeserver.repository.EmployerInfoRepository
import kz.scope.hiremeserver.repository.LogRepository
import kz.scope.hiremeserver.repository.UserRepository
import kz.scope.hiremeserver.security.CurrentUser
import kz.scope.hiremeserver.security.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.rmi.registry.LocateRegistry
import javax.validation.Valid

private val logger1 = LoggerFactory.getLogger(CompanyController::class.java)

@RestController
@RequestMapping("/api")
class CompanyController {

    @Autowired
    lateinit var logRepository: LogRepository

    var gson = Gson()

    @Autowired
    lateinit var companyRepository: CompanyRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var employerInfoRepository: EmployerInfoRepository

    @GetMapping("companies/{id}")
    fun getCompanyProfile(@PathVariable(value = "id") id: Long): CompanyProfile {
        logRepository.save(Log(
                controller = "CompanyController",
                methodName = "getCompanyProfile",
                httpMethod = "GET",
                urlMapping = "/companies/{id}",
                protected = false,
                requestBody = "{}",
                requestParam = "{id: }" + id)
        )
        val companyOptional = companyRepository.findById(id)
        val company: Company

        if (companyOptional.isPresent) company = companyOptional.get()
        else throw ResourceNotFoundException("Company", "id", id)

        val companyCreator = CompanyCreator(
                company.manager.user.username,
                company.manager.user.fullname,
                company.manager.manager_role,
                company.manager.user.userInfo.linked_in)

        return CompanyProfile(company.id,
                company.name,
                company.location,
                company.specialization,
                company.numEmployees,
                company.experience,
                company.hidden,
                Urls(
                        company.github,
                        company.linked_in,
                        company.web),

                companyCreator,
                company.logo,
                company.description,
                company.createdAt)
    }

    @GetMapping("companies/find")
    fun getCompanyProfile(@RequestParam(value = "name", required = true) name: String,
                          @RequestParam(value = "location", required = true) location: String): List<CompanyProfile> {
        logRepository.save(Log(
                controller = "CompanyController",
                methodName = "getCompanyProfile",
                httpMethod = "GET",
                urlMapping = "/companies/find",
                protected = false,
                requestBody = "{}",
                requestParam = "{name: " + name + ", location: " + location + "}")
        )

        val companyList = companyRepository.findByNameAndLocation(name, location)

        if (companyList.isEmpty()) throw ResourceNotFoundException("Company", "name", name)

        val companyProfiles: MutableList<CompanyProfile> = ArrayList<CompanyProfile>()
        for (company in companyList) {
            val companyCreator = CompanyCreator(
                    company.manager.user.username,
                    company.manager.user.fullname,
                    company.manager.manager_role,
                    company.manager.user.userInfo.linked_in)
            companyProfiles.add(CompanyProfile(company.id,
                    company.name,
                    company.location,
                    company.specialization,
                    company.numEmployees,
                    company.experience,
                    company.hidden,
                    Urls(
                            company.github,
                            company.linked_in,
                            company.web),

                    companyCreator,
                    company.logo,
                    company.description,
                    company.createdAt))
        }

        return companyProfiles
    }

    @PostMapping("company")
    @PreAuthorize("hasRole('USER')")
    fun createCompany(@CurrentUser currentUser: UserPrincipal, @Valid @RequestBody companyRequest: CompanyRequest) : ResponseEntity<*> {
        logRepository.save(Log(
                controller = "CompanyController",
                methodName = "createCompany",
                httpMethod = "POST",
                urlMapping = "/company",
                protected = true,
                requestBody = gson.toJson(companyRequest),
                requestParam = "{}")
        )

        val user = userRepository.findByUsername(companyRequest.creator.username)

        if (user == null) {
            return ResponseEntity(ApiResponse(false, "Such creator does not exists"), HttpStatus.EXPECTATION_FAILED)
        } else if (user.id != currentUser.id) {
            return ResponseEntity(ApiResponse(false, "You can only register a company on your account."), HttpStatus.UNAUTHORIZED)
        }

        val manager = EmployerInfo(user, companyRequest.creator.role)
        user.managing.add(manager)

        val company = Company(manager, companyRequest.name, companyRequest.location, companyRequest.logo_url,
                companyRequest.employee_number, companyRequest.specialization, companyRequest.description)

        employerInfoRepository.save(manager)
        val result = companyRepository.save(company)

        val location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/companies/{id}")
                .buildAndExpand(result.id).toUri()

        return ResponseEntity.created(location).body(ApiResponse(true, "Company registered successfully"))
    }

	@GetMapping("/my-companies")
    @PreAuthorize("hasRole('USER')")
    fun getMyCompanies(@CurrentUser currentUser: UserPrincipal) : List<CompanySummary> {
        val userOptional = userRepository.findById(currentUser.id)
        val user: User
        val companies: MutableList<CompanySummary> = ArrayList()

        if (userOptional.isPresent) {
            user = userOptional.get()
        } else {
            throw ResourceNotFoundException("User", "id", currentUser.id)
        }

        for (employer in user.managing) {
            companies.add(CompanySummary(
                    id = employer.company.id,
                    name = employer.company.name,
                    description = employer.company.description))
        }
        return companies
    }

}
