import re
import os

filepath = r"D:\Android\AndroidStudioProjects\kmp-shop\core\navigation\src\commonMain\kotlin\com\kazemieh\navigation\AppNavigation.kt"
with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# We will define the groups of screens
groups = {
    "AdminNavigation.kt": {
        "function_name": "adminNavGraph",
        "screens": [
            "AdminPanel", "ManageWallets", "ManageWithdrawals", "ManageOrders", 
            "ManageOptions", "ManageProduct", "ManageDiscounts", "ManageStories", 
            "AdminBlogList", "ManageBlog"
        ]
    },
    "AcademyNavigation.kt": {
        "function_name": "academyNavGraph",
        "screens": [
            "CourseCatalog", "CourseDetail", "CourseLearn", "CourseQuiz", 
            "Certificates", "CourseRequests", "LessonQuiz", "ProjectSubmission", 
            "PeerReview", "CertificateVerify", "PlacementQuiz", "CoursesByLevel", 
            "FreeCourses", "InstructorCourses"
        ]
    },
    "ClinicNavigation.kt": {
        "function_name": "clinicNavGraph",
        "screens": [
            "TherapistCatalog", "TherapistDetail", "MessagingThread", "Homework", 
            "Journal", "TherapistMatch", "MyAppointments", "MoodCheckIn", 
            "EmergencyResources", "SessionReceipt"
        ]
    },
    "PsychTestNavigation.kt": {
        "function_name": "psychTestNavGraph",
        "screens": [
            "PsychTestCatalog", "TakeTest"
        ]
    },
    "OrdersNavigation.kt": {
        "function_name": "ordersNavGraph",
        "screens": [
            "RecurringOrders", "MyOrders", "OrderDetail", "OrderTracking", 
            "ReturnRequest", "Checkout", "PaymentCompleted"
        ]
    },
    "ProfileNavigation.kt": {
        "function_name": "profileNavGraph",
        "screens": [
            "Referral", "Membership", "Profile", "CustomerClub", 
            "MyCourses", "Favorites", "Wallet", "Settings", "ContactUs"
        ]
    },
    "CatalogNavigation.kt": {
        "function_name": "catalogNavGraph",
        "screens": [
            "BundleList", "BundleDetail", "Comparison", "ShoppingAssistant", 
            "BlogList", "BlogDetail", "ProductDetail", "CategorySearch"
        ]
    }
}

# We need to extract the imports so we can put them in the new files.
# And we need to extract the composable blocks.

imports = []
for line in content.split("\n"):
    if line.startswith("import "):
        imports.append(line)

# For each composable, we extract it.
# A composable block looks like:
#         composable<Screen.X> { ... }
# We can find them by looking for `composable<Screen.`

blocks = {}

lines = content.split('\n')
i = 0
while i < len(lines):
    line = lines[i]
    match = re.search(r'composable<Screen\.([A-Za-z0-9_]+)>', line)
    if match:
        screen = match.group(1)
        # read the block
        # count braces
        braces = 0
        block_lines = []
        if '{' in line:
            braces += line.count('{') - line.count('}')
        block_lines.append(line)
        i += 1
        while braces > 0 and i < len(lines):
            braces += lines[i].count('{') - lines[i].count('}')
            block_lines.append(lines[i])
            i += 1
        blocks[screen] = '\n'.join(block_lines)
        continue
    i += 1

# write new files
base_dir = os.path.dirname(filepath)

for filename, info in groups.items():
    func_name = info["function_name"]
    screens = info["screens"]
    
    file_content = "package com.kazemieh.navigation\n\n"
    file_content += "import androidx.navigation.NavGraphBuilder\n"
    file_content += "import androidx.navigation.NavController\n"
    file_content += "import androidx.navigation.compose.composable\n"
    file_content += "import androidx.navigation.toRoute\n"
    file_content += "import com.kazemieh.common.Screen\n"
    
    # Add other imports that might be needed by these blocks
    # A simple heuristic: include all imports that are used in the blocks
    used_imports = []
    block_text = ""
    for screen in screens:
        if screen in blocks:
            block_text += blocks[screen] + "\n\n"
            
    for imp in imports:
        # get the class name
        cls = imp.split('.')[-1].strip()
        if cls in block_text:
            file_content += imp + "\n"
            
    file_content += "\nfun NavGraphBuilder." + func_name + "(navController: NavController) {\n"
    for screen in screens:
        if screen in blocks:
            file_content += blocks[screen] + "\n\n"
    file_content += "}\n"
    
    with open(os.path.join(base_dir, filename), 'w', encoding='utf-8') as f:
        f.write(file_content)

# Now rewrite AppNavigation.kt
new_app_nav = []
i = 0
in_nav_host = False
while i < len(lines):
    line = lines[i]
    if "NavHost(" in line:
        in_nav_host = True
    
    if in_nav_host and "authNavGraph(navController)" in line:
        new_app_nav.append(line)
        # insert new graphs
        for info in groups.values():
            new_app_nav.append("        " + info["function_name"] + "(navController)")
        i += 1
        continue
        
    match = re.search(r'composable<Screen\.([A-Za-z0-9_]+)>', line)
    if match:
        screen = match.group(1)
        if screen == "HomeGraph":
            # Keep HomeGraph
            pass
        else:
            # Skip this block
            braces = 0
            if '{' in line:
                braces += line.count('{') - line.count('}')
            i += 1
            while braces > 0 and i < len(lines):
                braces += lines[i].count('{') - lines[i].count('}')
                i += 1
            continue
            
    new_app_nav.append(line)
    i += 1

with open(filepath, 'w', encoding='utf-8') as f:
    f.write('\n'.join(new_app_nav))
