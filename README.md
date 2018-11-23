# hireme-server

## Running

```bash
     $ mvn spring-boot:run
```

By default, it will run on port 5000. And use MySQL Database on `178.128.170.25:3306`.
You can change it in [application.properties](src/main/resources/application.properties) file

## API specification

### Authorization

`/api/auth`

| Method | Path           | Request                                                   | Response                   | Protected |
| :----: | :------------- | --------------------------------------------------------- | -------------------------- | :-------: |
|  POST  | `/auth/signin` | RequestBody: <br> `{usernameOrEmail, password}`           | `{accessToken, tokenType}` |     No    |
|  POST  | `/auth/signup` | RequestBody: <br> `{fullname, username, email, password}` | `{success, message}`       |     No    |

### User

`/api/user`, `/api/users`

| Method | Path                              | Request                        | Response               | Protected |
| :----: | :-------------------------------- | ------------------------------ | ---------------------- | :-------: |
|   GET  | `/user/me`                        | --                             | `{username, fullname}` |    Yes    |
|  POST  | `/user/me`                        | RequestBody: <br>_UserInfo_    | `{success, message}`   |    Yes    |
|   GET  | `/user/checkUsernameAvailability` | RequestParam: <br>`{username}` | `{isAvailable}`        |     No    |
|   GET  | `/user/checkEmailAvailability`    | RequestParam: <br>`{email}`    | `{isAvailable}`        |     No    |
|   GET  | `/users/{username}`               | PathVariable: <br>`{username}` | _UserInfo_             |     No    |

### Company

`/api/company`

| Method | Path              | Request                             | Response                   | Protected |
| :----: | :---------------- | ----------------------------------- | -------------------------- | :-------: |
|  POST  | `/company`        | RequestBody: <br>_CompanyInfo_      | `{success, message, id}`   |    Yes    |
|   GET  | `/companies/{id}` | PathVariable: <br>`{id}`            | _CompanyInfo_              |     No    |
|   GET  | `/companies/find` | RequestBody: <br>`{name, location}` | [_CompanyInfo_]            |     No    |

```js
UserInfo = {
  username,            
  fullname,
  location,
  employment: {         // Optional
    position,
    company
  },
  current_role,         // SE, designer, marketing, sales, management, other
  education: {          // Optional
    university,         // name,              'Nazarbayev University'
    graduation_year,    // graduation year    2020
    graduation_month,   // graduation month   'June'
    major,              //                    'Computer Science'
    degree,             // Bachelor, Master, PhD
  },
  hidden,               // Show in search     true
  job_type,             // Full-time/Part-time/Contractor/Intern
  job_field,            // [Frontend dev, backend dev, web dev, mobile]
  skills,               // [react, spring, kotlin, java, tensorflow]
  createdAt,            // Date when entry was created
}

CompanyInfo = {
  id,                   // Only in response
  name,                 // Google
  creator: {
    username,           // m_nny
    role                // Founder/HR/Team leader
  },
  logo_url,             // https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png
  location,             // Astana, Kazakhstan
  employee_number,      // 100+
  specialization,       // Search Engine
  description,           // Best search engine in the world
  createdAt,            // Date when entry was created
}
```

### Job Offer

`/api/job-offer`

| Method | Path                          | Request                             | Response                   | Protected |
| :----: | :---------------------------- | ----------------------------------- | -------------------------- | :-------: |
|  POST  | `/job-offer`                  | RequestBody: <br>_JobOfferInfo_     | `{success, message, id}`   |    Yes    |
|   GET  | `/job-offers/{id}`            | PathVariable: <br>`{id}`            | _JobOfferInfo_             |    Yes    |
|   GET  | `/job-offers/find-by-company` | RequestBody: <br>`{company_id}`     | [_JobOfferInfo_]           |    Yes    |
|   GET  | `/job-offers/find-by-position`| RequestBody: <br>`{position}`       | [_JobOfferInfo_]           |    Yes    |
|   GET  | `/job-offers/find-by-location`| RequestBody: <br>`{location}`       | [_JobOfferInfo_]           |    Yes    |

```js
JobOfferInfo = {
  id,                   // only in response
  company: {
      company_id,
      name,             // only in response
      logo              // only in response
  },
  position,
  responsibilities,          
  qualifications,
  [location]            // list of locations              
  created_at,           // only in response
  updated_at            // only in response
}
```

### Posts (news feed) - returns the last 10 posts for now

`/api/post`

| Method | Path               | Request                          | Response                   | Protected |
| :----: | :------------------| -------------------------------- | -------------------------- | :-------: |
|  POST  | `/post`            | RequestBody: <br>_PostForm_      | `{success, message, id}`   |    Yes    |
|   GET  | `/posts/{id}`      | PathVariable: <br>`{id}`         | _Post_                     |    Yes    |
|   GET  | `/posts`           | --                               | [_Post_]                   |    Yes    |

Used in response
```js
Post = {
  id,                      
  company,                 // is company (true - author is company, false - author is user)
  author: {
      id,                  // company id for company or user id for user
      name                 // fullname for user or company name for company
  },                 
  title,
  text,
  jobOffers,               // list of JobOfferInfo
  created_at
}

```

Used in request
```js
PostForm = {
  company,                 // is company (true - author is company, false - author is user)
  author,                  // company id for company or user id for user
  title,
  text,
  jobOffersIds             // list of ids of job offers
}

```

### Following users

`/api`

| Method | Path                             | Request                                                          | Response                   | Protected |
| :----: | :--------------------------------| ---------------------------------------------------------------- | -------------------------- | :-------: |
|  POST  | `/{username}/follow`             | PathVariable: <br>`{username}`                                   | `{success, message, id}`   |    Yes    |
|   GET  | `/{username}/followers`          | PathVariable: <br>`{username}`                                   | [_UserSummary_]            |    Yes    |
|   GET  | `/{username}/following`          | PathVariable: <br>`{username}`                                   | [_UserSummary_]            |    Yes    |
|   GET  | `/{username}/following-companies`| PathVariable: <br>`{username}`                                   | [_CompanySummary_]         |    Yes    |
|  POST  | `/{company_id}/follow-company`   | PathVariable: <br>`{company_id}`                                 | `{success, message, id}`   |    Yes    |
|   GET  | `/{company_id}/company-followers`| PathVariable: <br>`{company_id}`                                 | [_UserSummary_]            |    Yes    |
| DELETE | `/{username}/unfollow`           | PathVariable: <br>`{username}`                                   | `{success, message, id}`   |    Yes    |
| DELETE | `/{company_id}/unfollow-company` | PathVariable: <br>`{company_id}`                                 | `{success, message, id}`   |    Yes    |
|   GET  | `/{username}/is-following`       | PathVariable: <br>`{username}`                                   | _Boolean_                  |    Yes    |
|   GET  | `/{company_id}/is-following`     | PathVariable: <br>`{company_id}`                                 | _Boolean_                  |    Yes    |

Used only in response
```js
CompanySummary = {
    id,
    name,
    description
}

UserSummary = {
    id,
    username,
    fullname
}
```