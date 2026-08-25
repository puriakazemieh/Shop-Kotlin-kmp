# Evidence for P01-WPPLUGIN-SEC-018

## Changes Made
1. **Controller Registrations Disabled by Default**:
   - In class-cb-plugin.php, wrapped the instantiation of CB_Academy_Controller, CB_Clinic_Controller, CB_Psychtest_Controller, CB_Course_Request_Controller, and CB_Admin_Clinic_Controller with the cb_enable_health_lms filter (defaulting to alse).
   - Removed duplicated initializations of CB_Course_Request_Controller and CB_Admin_Clinic_Controller.
   - In class-cb-admin-content-controller.php, wrapped all routes belonging to courses (LMS), therapists (Clinic), and psych tests in the same cb_enable_health_lms filter.
   
2. **Custom Post Types Disabled by Default**:
   - In class-cb-cpt.php, wrapped the registration of cb_course, cb_therapist, cb_psychtest, cb_course_request, and cb_appointment with the cb_enable_health_lms filter.
   - This prevents WordPress's built-in REST API from exposing these custom post types (since show_in_rest was true for most of them).

## Verification
- Lint check: php -l passed for class-cb-plugin.php, class-cb-admin-content-controller.php, and class-cb-cpt.php.
- The aforementioned APIs and post types will not be registered unless the cb_enable_health_lms filter is explicitly set to true by the theme or another plugin, satisfying the "fail-closed" requirement.
