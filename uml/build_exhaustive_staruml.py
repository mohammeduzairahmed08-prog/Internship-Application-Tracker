import json
import os
import random

counter = 0

def gen_id(prefix="AAAAAAG"):
    global counter
    counter += 1
    chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    suffix = "".join(random.choice(chars) for _ in range(12))
    return f"{prefix}{counter:04d}{suffix}="

def make_label(id_val, parent_id, text, left, top, width, height, font="Arial;13;0", visible=True):
    return {
        "_type": "LabelView",
        "_id": id_val,
        "_parent": {"$ref": parent_id},
        "visible": visible,
        "font": font,
        "parentStyle": True,
        "left": left,
        "top": top,
        "width": width,
        "height": height,
        "text": text
    }

def make_name_compartment(parent_id, model_id, name, left, top, width, height=25):
    comp_id = gen_id()
    lbl_stereo = gen_id()
    lbl_name = gen_id()
    lbl_ns = gen_id()
    lbl_prop = gen_id()
    return {
        "_type": "UMLNameCompartmentView",
        "_id": comp_id,
        "_parent": {"$ref": parent_id},
        "model": {"$ref": model_id},
        "subViews": [
            make_label(lbl_stereo, comp_id, "", left, top, width, 13, visible=False),
            make_label(lbl_name, comp_id, name, left + 5, top + 5, width - 10, 13, font="Arial;13;1"),
            make_label(lbl_ns, comp_id, "", left, top, width, 13, visible=False),
            make_label(lbl_prop, comp_id, "", left, top, width, 13, visible=False)
        ],
        "font": "Arial;13;0",
        "parentStyle": True,
        "left": left,
        "top": top,
        "width": width,
        "height": height
    }

def make_list_compartment(parent_id, model_id, comp_type, left, top, width, height=10):
    comp_id = gen_id()
    return {
        "_type": comp_type,
        "_id": comp_id,
        "_parent": {"$ref": parent_id},
        "model": {"$ref": model_id},
        "visible": True,
        "font": "Arial;13;0",
        "parentStyle": True,
        "left": left,
        "top": top,
        "width": width,
        "height": height,
        "subViews": []
    }

# =========================================================================
# 1. SEQUENCE DIAGRAM (COMPREHENSIVE - 10 LIFELINES, 36 DETAILED MESSAGES)
# =========================================================================
def build_sequence_project():
    proj_id = "AAAAAAFF+h6SjaM2Hec="
    model_id = "AAAAAAFF+qBWK6M3Z8Y="
    collab_id = gen_id()
    inter_id = gen_id()
    diag_id = gen_id()

    lifelines_info = [
        ("Student: Applicant", 50, 140),
        (":WebBrowserUI", 230, 150),
        (":AuthController", 420, 150),
        (":ApplicationController", 610, 170),
        (":InterviewController", 820, 160),
        (":DashboardController", 1020, 160),
        (":ApplicationService", 1220, 160),
        (":InterviewService", 1420, 150),
        (":ApplicationRepository", 1610, 170),
        (":MySQLDatabase", 1820, 140)
    ]

    ll_models = []
    ll_views = []
    ll_map = {}
    total_height = 1750

    for name, lx, lw in lifelines_info:
        lid = gen_id()
        lvid = gen_id()
        line_part_id = gen_id()
        ll_map[name] = {"model_id": lid, "view_id": lvid, "line_part_id": line_part_id, "x": lx, "w": lw}

        ll_models.append({
            "_type": "UMLLifeline",
            "_id": lid,
            "_parent": {"$ref": inter_id},
            "name": name
        })

        nc = make_name_compartment(lvid, lid, name, lx, 40, lw, 35)
        line_part = {
            "_type": "UMLLinePartView",
            "_id": line_part_id,
            "_parent": {"$ref": lvid},
            "model": {"$ref": lid},
            "font": "Arial;13;0",
            "left": lx + lw // 2,
            "top": 75,
            "width": 1,
            "height": total_height - 75
        }

        ll_views.append({
            "_type": "UMLSeqLifelineView",
            "_id": lvid,
            "_parent": {"$ref": diag_id},
            "model": {"$ref": lid},
            "subViews": [nc, line_part],
            "font": "Arial;13;0",
            "left": lx,
            "top": 40,
            "width": lw,
            "height": total_height,
            "nameCompartment": {"$ref": nc["_id"]},
            "linePart": {"$ref": line_part_id}
        })

    messages_data = [
        # Phase 1: Authentication
        ("Student: Applicant", ":WebBrowserUI", "1: enterCredentials(email, password)", "synchCall", 100),
        (":WebBrowserUI", ":AuthController", "2: POST /api/auth/login", "synchCall", 140),
        (":AuthController", ":AuthController", "3: verifyBCryptPassword()", "synchCall", 180),
        (":AuthController", ":WebBrowserUI", "4: 200 OK (AuthSuccess & SessionCookie)", "reply", 220),
        # Phase 2: Dashboard KPIs & Reminders
        (":WebBrowserUI", ":DashboardController", "5: GET /api/dashboard/summary", "synchCall", 260),
        (":DashboardController", ":ApplicationService", "6: getDashboardSummary(userId)", "synchCall", 300),
        (":ApplicationService", ":ApplicationRepository", "7: countByStatus(userId)", "synchCall", 340),
        (":ApplicationRepository", ":MySQLDatabase", "8: SELECT status, COUNT(*) FROM applications WHERE user_id=?", "synchCall", 380),
        (":MySQLDatabase", ":ApplicationRepository", "9: return statusCounts", "reply", 420),
        (":ApplicationService", ":ApplicationRepository", "10: findUpcomingDeadlines(userId, now, now+7Days)", "synchCall", 460),
        (":ApplicationRepository", ":MySQLDatabase", "11: SELECT * FROM applications WHERE deadline <= now+7", "synchCall", 500),
        (":MySQLDatabase", ":ApplicationRepository", "12: return deadlineList", "reply", 540),
        (":ApplicationRepository", ":ApplicationService", "13: return aggregatedData", "reply", 580),
        (":ApplicationService", ":DashboardController", "14: return DashboardSummaryDTO", "reply", 620),
        (":DashboardController", ":WebBrowserUI", "15: 200 OK (JSON Metrics & 7-Day Alerts)", "reply", 660),
        (":WebBrowserUI", "Student: Applicant", "16: renderDashboardChartsAndReminders()", "reply", 700),
        # Phase 3: Application Creation & Validation
        ("Student: Applicant", ":WebBrowserUI", "17: submitNewApplication(company, role, deadline, stipend)", "synchCall", 740),
        (":WebBrowserUI", ":ApplicationController", "18: POST /api/applications", "synchCall", 780),
        (":ApplicationController", ":ApplicationService", "19: createApplication(userId, reqDto)", "synchCall", 820),
        (":ApplicationService", ":ApplicationService", "20: validateDatesAndOwnership()", "synchCall", 860),
        (":ApplicationService", ":ApplicationRepository", "21: save(applicationEntity)", "synchCall", 900),
        (":ApplicationRepository", ":MySQLDatabase", "22: INSERT INTO applications(...) RETURNING id", "synchCall", 940),
        (":MySQLDatabase", ":ApplicationRepository", "23: return recordId", "reply", 980),
        (":ApplicationRepository", ":ApplicationService", "24: return savedEntity", "reply", 1020),
        (":ApplicationService", ":ApplicationController", "25: return ApplicationResponse", "reply", 1060),
        (":ApplicationController", ":WebBrowserUI", "26: 201 Created (JSON)", "reply", 1100),
        # Phase 4: Interview Scheduling & Status Transition
        ("Student: Applicant", ":WebBrowserUI", "27: scheduleInterview(appId, round, date, link)", "synchCall", 1140),
        (":WebBrowserUI", ":InterviewController", "28: POST /api/applications/{id}/interviews", "synchCall", 1180),
        (":InterviewController", ":InterviewService", "29: scheduleInterview(appId, interviewDto)", "synchCall", 1220),
        (":InterviewService", ":ApplicationRepository", "30: updateStatus(INTERVIEW_SCHEDULED)", "synchCall", 1260),
        (":InterviewService", ":MySQLDatabase", "31: INSERT INTO interviews & UPDATE status", "synchCall", 1300),
        (":MySQLDatabase", ":InterviewService", "32: return interviewId", "reply", 1340),
        (":InterviewService", ":InterviewController", "33: return InterviewResponse", "reply", 1380),
        (":InterviewController", ":WebBrowserUI", "34: 201 Created (UpdatedTimeline)", "reply", 1420),
        (":WebBrowserUI", "Student: Applicant", "35: showUpcomingInterviewReminderToast()", "reply", 1460),
        # Phase 5: Search & Filter
        ("Student: Applicant", ":WebBrowserUI", "36: filterApplications(status, workMode, location)", "synchCall", 1500),
        (":WebBrowserUI", ":ApplicationController", "37: GET /api/applications?status=...&workMode=...", "synchCall", 1540),
        (":ApplicationController", ":ApplicationService", "38: getApplicationsWithSpecification()", "synchCall", 1580),
        (":ApplicationService", ":ApplicationRepository", "39: findAll(specification, pageable)", "synchCall", 1620),
        (":ApplicationRepository", ":ApplicationController", "40: return Page<ApplicationResponse>", "reply", 1660),
        (":ApplicationController", ":WebBrowserUI", "41: 200 OK (FilteredTable)", "reply", 1700)
    ]

    msg_models = []
    msg_views = []

    for sname, tname, msg_text, msort, my in messages_data:
        sm = ll_map[sname]
        tm = ll_map[tname]
        mid = gen_id()
        mvid = gen_id()

        msg_models.append({
            "_type": "UMLMessage",
            "_id": mid,
            "_parent": {"$ref": inter_id},
            "name": msg_text,
            "source": {"$ref": sm["model_id"]},
            "target": {"$ref": tm["model_id"]},
            "messageSort": msort
        })

        x1 = sm["x"] + sm["w"] // 2
        x2 = tm["x"] + tm["w"] // 2
        if x1 == x2:
            pts = f"{x1}:{my};{x1 + 40}:{my};{x1 + 40}:{my + 25};{x1}:{my + 25}"
        else:
            pts = f"{x1}:{my};{x2}:{my}"

        lbl_id = gen_id()
        lbl_stereo = gen_id()
        lbl_prop = gen_id()
        act_id = gen_id()

        msg_views.append({
            "_type": "UMLSeqMessageView",
            "_id": mvid,
            "_parent": {"$ref": diag_id},
            "model": {"$ref": mid},
            "subViews": [
                {"_type": "EdgeLabelView", "_id": lbl_id, "_parent": {"$ref": mvid}, "model": {"$ref": mid}, "text": msg_text, "visible": True, "font": "Arial;13;0", "left": min(x1, x2) + 15, "top": my - 15, "height": 13, "alpha": 1.57, "distance": 10, "edgePosition": 1},
                {"_type": "EdgeLabelView", "_id": lbl_stereo, "_parent": {"$ref": mvid}, "model": {"$ref": mid}, "visible": False, "font": "Arial;13;0", "left": 0, "top": 0, "height": 13},
                {"_type": "EdgeLabelView", "_id": lbl_prop, "_parent": {"$ref": mvid}, "model": {"$ref": mid}, "visible": False, "font": "Arial;13;0", "left": 0, "top": 0, "height": 13},
                {"_type": "UMLActivationView", "_id": act_id, "_parent": {"$ref": mvid}, "model": {"$ref": mid}, "visible": True, "font": "Arial;13;0", "left": x2 - 7, "top": my, "width": 14, "height": 28}
            ],
            "font": "Arial;13;0",
            "head": {"$ref": tm["line_part_id"]},
            "tail": {"$ref": sm["line_part_id"]},
            "lineStyle": 2,
            "points": pts,
            "nameLabel": {"$ref": lbl_id},
            "activation": {"$ref": act_id}
        })

    diagram = {
        "_type": "UMLSequenceDiagram",
        "_id": diag_id,
        "_parent": {"$ref": inter_id},
        "name": "Sequence Diagram",
        "defaultDiagram": True,
        "ownedViews": ll_views + msg_views
    }

    interaction = {
        "_type": "UMLInteraction",
        "_id": inter_id,
        "_parent": {"$ref": collab_id},
        "name": "InternshipTrackingFullLifecycleInteraction",
        "lifelines": ll_models,
        "messages": msg_models,
        "ownedElements": [diagram]
    }

    collab = {
        "_type": "UMLCollaboration",
        "_id": collab_id,
        "_parent": {"$ref": model_id},
        "name": "Collaboration",
        "ownedElements": [interaction]
    }

    model = {
        "_type": "UMLModel",
        "_id": model_id,
        "_parent": {"$ref": proj_id},
        "name": "SequenceModel",
        "ownedElements": [collab]
    }

    return {
        "_type": "Project",
        "_id": proj_id,
        "name": "Internship Application Tracker",
        "documentVersion": 1,
        "ownedElements": [model]
    }

# =========================================================================
# 2. COMPONENT DIAGRAM (COMPREHENSIVE - 6 TIERS, 18 ARTIFACTS & INTERFACES)
# =========================================================================
def build_component_project():
    proj_id = "AAAAAAFF+h6SjaM2Hec="
    model_id = "AAAAAAFF+qBWK6M3Z8Y="
    diag_id = gen_id()

    components_info = [
        # Presentation Tier
        ("WebBrowserUI", 60, 90, 260, 95),
        ("ChartJsVisualizer", 60, 220, 260, 95),
        ("RestClientApiJs", 60, 350, 260, 95),
        # REST Controller Tier
        ("AuthController", 390, 70, 240, 90),
        ("ApplicationController", 390, 180, 240, 90),
        ("InterviewController", 390, 290, 240, 90),
        ("DashboardController", 390, 400, 240, 90),
        ("GlobalExceptionHandler", 390, 510, 240, 85),
        # Security Subsystem
        ("SecurityFilterChain", 700, 70, 250, 90),
        ("BCryptPasswordEncoder", 700, 180, 250, 85),
        # Service Tier
        ("AuthService", 700, 285, 250, 90),
        ("ApplicationService", 700, 395, 250, 90),
        ("InterviewService", 700, 505, 250, 90),
        ("DashboardService", 700, 615, 250, 90),
        # Persistence Tier
        ("UserRepository", 1020, 70, 250, 85),
        ("ApplicationRepository", 1020, 180, 250, 85),
        ("InterviewRepository", 1020, 290, 250, 85),
        ("Hibernate6ORM", 1020, 400, 250, 95),
        # Database Tier
        ("MySQLDatabaseServer", 650, 750, 380, 110)
    ]

    comp_models = []
    comp_views = []
    comp_map = {}

    for cname, cx, cy, cw, ch in components_info:
        cid = gen_id()
        cvid = gen_id()
        comp_map[cname] = {"model_id": cid, "view_id": cvid, "x": cx, "y": cy, "w": cw, "h": ch}

        comp_models.append({
            "_type": "UMLComponent",
            "_id": cid,
            "_parent": {"$ref": model_id},
            "name": cname,
            "ownedElements": []
        })

        nc = make_name_compartment(cvid, cid, cname, cx, cy, cw, 25)
        attr_comp = make_list_compartment(cvid, cid, "UMLAttributeCompartmentView", cx, cy + 25, cw, 10)
        op_comp = make_list_compartment(cvid, cid, "UMLOperationCompartmentView", cx, cy + 45, cw, 10)
        rec_comp = make_list_compartment(cvid, cid, "UMLReceptionCompartmentView", cx, cy + 65, cw, 10)
        tpl_comp = make_list_compartment(cvid, cid, "UMLTemplateParameterCompartmentView", cx, cy, 0, 0)
        tpl_comp["visible"] = False

        comp_views.append({
            "_type": "UMLComponentView",
            "_id": cvid,
            "_parent": {"$ref": diag_id},
            "model": {"$ref": cid},
            "subViews": [nc, attr_comp, op_comp, rec_comp, tpl_comp],
            "font": "Arial;13;0",
            "left": cx,
            "top": cy,
            "width": cw,
            "height": ch,
            "nameCompartment": {"$ref": nc["_id"]},
            "attributeCompartment": {"$ref": attr_comp["_id"]},
            "operationCompartment": {"$ref": op_comp["_id"]},
            "receptionCompartment": {"$ref": rec_comp["_id"]},
            "templateParameterCompartment": {"$ref": tpl_comp["_id"]}
        })

    dependencies_data = [
        # Presentation to Controllers
        ("WebBrowserUI", "AuthController", "POST /api/auth/login", "320:115;390:115"),
        ("RestClientApiJs", "ApplicationController", "CRUD /api/applications", "320:390;390:225"),
        ("RestClientApiJs", "InterviewController", "POST /api/interviews", "320:410;390:335"),
        ("ChartJsVisualizer", "DashboardController", "GET /api/dashboard", "320:265;390:445"),
        # Controllers to Security & Services
        ("AuthController", "SecurityFilterChain", "authenticate()", "630:115;700:115"),
        ("SecurityFilterChain", "BCryptPasswordEncoder", "matches(raw, hash)", "825:160;825:180"),
        ("AuthController", "AuthService", "registerUser()", "630:130;700:300"),
        ("ApplicationController", "ApplicationService", "createApp() / update()", "630:225;700:440"),
        ("InterviewController", "InterviewService", "scheduleInterview()", "630:335;700:550"),
        ("DashboardController", "DashboardService", "computeKPIs()", "630:445;700:660"),
        ("GlobalExceptionHandler", "ApplicationController", "catches exceptions", "510:510;510:270"),
        # Services to Repositories
        ("AuthService", "UserRepository", "findByEmail()", "950:330;1020:110"),
        ("ApplicationService", "ApplicationRepository", "save() & findAll(spec)", "950:440;1020:225"),
        ("InterviewService", "InterviewRepository", "save() & findByAppId()", "950:550;1020:335"),
        ("DashboardService", "ApplicationRepository", "countByStatus() & deadlines", "950:660;1020:265"),
        # Repositories to Hibernate ORM
        ("UserRepository", "Hibernate6ORM", "UserEntity Mapping", "1140:155;1140:400"),
        ("ApplicationRepository", "Hibernate6ORM", "ApplicationEntity Mapping", "1140:265;1140:400"),
        ("InterviewRepository", "Hibernate6ORM", "InterviewEntity Cascade", "1140:375;1140:400"),
        # Hibernate ORM to MySQL Database
        ("Hibernate6ORM", "MySQLDatabaseServer", "JDBC Port 3306 (SQL Statements & Pool)", "1020:460;900:750")
    ]

    dep_views = []
    for c1, c2, dep_text, pts in dependencies_data:
        cm1 = comp_map[c1]
        cm2 = comp_map[c2]
        did = gen_id()
        dvid = gen_id()

        dep_model = {
            "_type": "UMLDependency",
            "_id": did,
            "_parent": {"$ref": cm1["model_id"]},
            "name": dep_text,
            "source": {"$ref": cm1["model_id"]},
            "target": {"$ref": cm2["model_id"]}
        }

        for cm in comp_models:
            if cm["_id"] == cm1["model_id"]:
                cm["ownedElements"].append(dep_model)
                break

        lbl_id = gen_id()
        dep_views.append({
            "_type": "UMLDependencyView",
            "_id": dvid,
            "_parent": {"$ref": diag_id},
            "model": {"$ref": did},
            "subViews": [
                {"_type": "EdgeLabelView", "_id": lbl_id, "_parent": {"$ref": dvid}, "model": {"$ref": did}, "text": dep_text, "visible": True, "font": "Arial;13;0", "left": 0, "top": 0, "height": 13, "alpha": 1.57, "distance": 15, "edgePosition": 1}
            ],
            "font": "Arial;13;0",
            "head": {"$ref": cm2["view_id"]},
            "tail": {"$ref": cm1["view_id"]},
            "lineStyle": 1,
            "points": pts,
            "nameLabel": {"$ref": lbl_id}
        })

    diagram = {
        "_type": "UMLComponentDiagram",
        "_id": diag_id,
        "_parent": {"$ref": model_id},
        "name": "Component Diagram",
        "defaultDiagram": True,
        "ownedViews": comp_views + dep_views
    }

    model = {
        "_type": "UMLModel",
        "_id": model_id,
        "_parent": {"$ref": proj_id},
        "name": "ComponentModel",
        "ownedElements": [diagram] + comp_models
    }

    return {
        "_type": "Project",
        "_id": proj_id,
        "name": "Internship Application Tracker",
        "documentVersion": 1,
        "ownedElements": [model]
    }

# =========================================================================
# 3. COLLABORATION DIAGRAM (COMPREHENSIVE - 13 LIFELINES, 22 COMMUNICATION FLOWS)
# =========================================================================
def build_collaboration_project():
    proj_id = "AAAAAAFF+h6SjaM2Hec="
    model_id = "AAAAAAFF+qBWK6M3Z8Y="
    collab_id = gen_id()
    inter_id = gen_id()
    diag_id = gen_id()

    lifelines_info = [
        (":StudentClient", 60, 240, 150, 50),
        (":AuthController", 320, 80, 160, 50),
        (":ApplicationController", 320, 240, 180, 50),
        (":InterviewController", 320, 400, 160, 50),
        (":DashboardController", 320, 560, 170, 50),
        (":AuthService", 620, 80, 150, 50),
        (":ApplicationService", 620, 240, 170, 50),
        (":InterviewService", 620, 400, 160, 50),
        (":DashboardService", 620, 560, 170, 50),
        (":UserRepository", 920, 80, 160, 50),
        (":ApplicationRepository", 920, 240, 180, 50),
        (":InterviewRepository", 920, 400, 170, 50),
        (":MySQLDatabase", 1200, 320, 170, 70)
    ]

    ll_models = []
    ll_views = []
    ll_map = {}

    for ll_name, ll_x, ll_y, ll_w, ll_h in lifelines_info:
        lid = gen_id()
        lvid = gen_id()
        ll_map[ll_name] = {"model_id": lid, "view_id": lvid, "x": ll_x, "y": ll_y, "w": ll_w, "h": ll_h}

        ll_models.append({
            "_type": "UMLLifeline",
            "_id": lid,
            "_parent": {"$ref": inter_id},
            "name": ll_name
        })

        nc = make_name_compartment(lvid, lid, ll_name, ll_x, ll_y, ll_w, 25)
        ll_views.append({
            "_type": "UMLCommLifelineView",
            "_id": lvid,
            "_parent": {"$ref": diag_id},
            "model": {"$ref": lid},
            "subViews": [nc],
            "font": "Arial;13;0",
            "left": ll_x,
            "top": ll_y,
            "width": ll_w,
            "height": ll_h,
            "nameCompartment": {"$ref": nc["_id"]}
        })

    paths_data = [
        # Client -> Controllers
        (":StudentClient", ":AuthController", "loginLink", "210:245;320:115", [
            ("1: login(credentials)", "synchCall"),
            ("1.3: register(newUser)", "synchCall")
        ]),
        (":StudentClient", ":ApplicationController", "appLink", "210:265;320:265", [
            ("2: createApplication(dto)", "synchCall"),
            ("5: updateStatus(id, status)", "synchCall")
        ]),
        (":StudentClient", ":InterviewController", "interviewLink", "210:285;320:415", [
            ("3: scheduleInterview(dto)", "synchCall"),
            ("6: recordOutcome(outcome)", "synchCall")
        ]),
        (":StudentClient", ":DashboardController", "dashboardLink", "180:290;320:575", [
            ("4: getSummaryStats()", "synchCall")
        ]),
        # Controllers -> Services
        (":AuthController", ":AuthService", "authServiceLink", "480:105;620:105", [
            ("1.1: verifyBCryptPassword()", "synchCall"),
            ("1.4: checkDuplicateEmail()", "synchCall")
        ]),
        (":ApplicationController", ":ApplicationService", "appServiceLink", "500:265;620:265", [
            ("2.1: validateAndSave(dto)", "synchCall"),
            ("5.1: updateApplicationStatus(id)", "synchCall")
        ]),
        (":InterviewController", ":InterviewService", "interviewServiceLink", "480:425;620:425", [
            ("3.1: recordInterviewSchedule(dto)", "synchCall"),
            ("6.1: updateOutcomeAndNotes()", "synchCall")
        ]),
        (":DashboardController", ":DashboardService", "dashboardServiceLink", "490:585;620:585", [
            ("4.1: computeKPIsAndAlerts()", "synchCall")
        ]),
        # Services -> Repositories
        (":AuthService", ":UserRepository", "userRepoLink", "770:105;920:105", [
            ("1.2: findByEmail(email)", "synchCall"),
            ("1.5: save(UserEntity)", "synchCall")
        ]),
        (":ApplicationService", ":ApplicationRepository", "appRepoLink", "790:265;920:265", [
            ("2.2: save(Application)", "synchCall"),
            ("5.2: update(Application)", "synchCall")
        ]),
        (":InterviewService", ":InterviewRepository", "interviewRepoLink", "780:425;920:425", [
            ("3.2: save(Interview)", "synchCall")
        ]),
        (":InterviewService", ":ApplicationRepository", "interviewAppRepoLink", "770:400;920:285", [
            ("3.3: updateStatus(SCHEDULED)", "synchCall")
        ]),
        (":DashboardService", ":ApplicationRepository", "dashboardRepoLink", "780:560;940:290", [
            ("4.2: countByStatus() & findDeadlines()", "synchCall")
        ]),
        # Repositories -> Database
        (":UserRepository", ":MySQLDatabase", "userDbLink", "1080:105;1200:325", [
            ("1.6: INSERT / SELECT users", "synchCall")
        ]),
        (":ApplicationRepository", ":MySQLDatabase", "appDbLink", "1100:265;1200:345", [
            ("2.3: INSERT INTO applications", "synchCall"),
            ("4.3: SELECT COUNT(*) & DEADLINES", "synchCall")
        ]),
        (":InterviewRepository", ":MySQLDatabase", "interviewDbLink", "1090:425;1200:375", [
            ("3.4: INSERT INTO interviews", "synchCall")
        ])
    ]

    msg_models = []
    path_views = []
    msg_views = []
    connectors = []

    for sname, tname, conn_name, pts, msgs in paths_data:
        sm = ll_map[sname]
        tm = ll_map[tname]
        conn_id = gen_id()
        path_vid = gen_id()

        connector = {
            "_type": "UMLConnector",
            "_id": conn_id,
            "_parent": {"$ref": collab_id},
            "name": conn_name,
            "end1": {
                "_type": "UMLConnectorEnd",
                "_id": gen_id(),
                "_parent": {"$ref": conn_id},
                "reference": {"$ref": sm["model_id"]}
            },
            "end2": {
                "_type": "UMLConnectorEnd",
                "_id": gen_id(),
                "_parent": {"$ref": conn_id},
                "reference": {"$ref": tm["model_id"]}
            }
        }
        connectors.append(connector)

        lbl_id = gen_id()
        path_view = {
            "_type": "UMLCommunicationPathView",
            "_id": path_vid,
            "_parent": {"$ref": diag_id},
            "model": {"$ref": conn_id},
            "subViews": [
                {"_type": "EdgeLabelView", "_id": lbl_id, "_parent": {"$ref": path_vid}, "model": {"$ref": conn_id}, "visible": False, "font": "Arial;13;0", "left": 0, "top": 0, "height": 13}
            ],
            "font": "Arial;13;0",
            "head": {"$ref": tm["view_id"]},
            "tail": {"$ref": sm["view_id"]},
            "lineStyle": 1,
            "points": pts,
            "nameLabel": {"$ref": lbl_id}
        }
        path_views.append(path_view)

        for msg_text, msort in msgs:
            mid = gen_id()
            mvid = gen_id()
            msg_models.append({
                "_type": "UMLMessage",
                "_id": mid,
                "_parent": {"$ref": inter_id},
                "name": msg_text,
                "source": {"$ref": sm["model_id"]},
                "target": {"$ref": tm["model_id"]},
                "connector": {"$ref": conn_id}
            })

            mlbl_id = gen_id()
            mstereo_id = gen_id()
            mprop_id = gen_id()

            msg_views.append({
                "_type": "UMLCommMessageView",
                "_id": mvid,
                "_parent": {"$ref": diag_id},
                "model": {"$ref": mid},
                "subViews": [
                    {"_type": "NodeLabelView", "_id": mlbl_id, "_parent": {"$ref": mvid}, "model": {"$ref": mid}, "text": msg_text, "visible": True, "font": "Arial;13;0", "left": 0, "top": 0, "height": 13},
                    {"_type": "NodeLabelView", "_id": mstereo_id, "_parent": {"$ref": mvid}, "model": {"$ref": mid}, "visible": False, "font": "Arial;13;0", "left": 0, "top": 0, "height": 13},
                    {"_type": "NodeLabelView", "_id": mprop_id, "_parent": {"$ref": mvid}, "model": {"$ref": mid}, "visible": False, "font": "Arial;13;0", "left": 0, "top": 0, "height": 13}
                ],
                "font": "Arial;13;0",
                "hostEdge": {"$ref": path_vid},
                "nameLabel": {"$ref": mlbl_id},
                "stereotypeLabel": {"$ref": mstereo_id},
                "propertyLabel": {"$ref": mprop_id}
            })

    diagram = {
        "_type": "UMLCommunicationDiagram",
        "_id": diag_id,
        "_parent": {"$ref": inter_id},
        "name": "Collaboration Diagram",
        "defaultDiagram": True,
        "ownedViews": ll_views + path_views + msg_views
    }

    interaction = {
        "_type": "UMLInteraction",
        "_id": inter_id,
        "_parent": {"$ref": collab_id},
        "name": "CommunicationInteraction",
        "lifelines": ll_models,
        "messages": msg_models,
        "ownedElements": [diagram]
    }

    collab = {
        "_type": "UMLCollaboration",
        "_id": collab_id,
        "_parent": {"$ref": model_id},
        "name": "Collaboration",
        "ownedElements": [interaction],
        "connectors": connectors
    }

    model = {
        "_type": "UMLModel",
        "_id": model_id,
        "_parent": {"$ref": proj_id},
        "name": "CollabModel",
        "ownedElements": [collab]
    }

    return {
        "_type": "Project",
        "_id": proj_id,
        "name": "Internship Application Tracker",
        "documentVersion": 1,
        "ownedElements": [model]
    }

# =========================================================================
# 4. CLASS DIAGRAM (COMPREHENSIVE - ENTITIES, ENUMS, SERVICES, CONTROLLERS, DTOS)
# =========================================================================
def build_class_project():
    proj_id = "AAAAAAFF+h6SjaM2Hec="
    model_id = "AAAAAAFF+qBWK6M3Z8Y="
    diag_id = gen_id()

    # Entities
    classes_data = [
        {
            "name": "User",
            "x": 60, "y": 70, "w": 230, "h": 190,
            "attrs": [
                ("- id", "Long"),
                ("- fullName", "String"),
                ("- email", "String"),
                ("- passwordHash", "String"),
                ("- createdAt", "LocalDateTime")
            ],
            "ops": [
                ("+ getId()", "Long"),
                ("+ getEmail()", "String"),
                ("+ getApplications()", "List<Application>")
            ]
        },
        {
            "name": "Application",
            "x": 370, "y": 70, "w": 300, "h": 320,
            "attrs": [
                ("- id", "Long"),
                ("- user", "User"),
                ("- companyName", "String"),
                ("- jobRole", "String"),
                ("- jobDescription", "String"),
                ("- applicationUrl", "String"),
                ("- location", "String"),
                ("- workMode", "WorkMode"),
                ("- applicationDate", "LocalDate"),
                ("- deadline", "LocalDate"),
                ("- stipend", "BigDecimal"),
                ("- status", "ApplicationStatus"),
                ("- priority", "Priority"),
                ("- notes", "String")
            ],
            "ops": [
                ("+ getId()", "Long"),
                ("+ updateStatus(status)", "void"),
                ("+ addInterview(interview)", "void"),
                ("+ isDeadlineApproaching()", "boolean")
            ]
        },
        {
            "name": "Interview",
            "x": 750, "y": 70, "w": 260, "h": 240,
            "attrs": [
                ("- id", "Long"),
                ("- application", "Application"),
                ("- interviewRound", "String"),
                ("- interviewDate", "LocalDateTime"),
                ("- interviewMode", "InterviewMode"),
                ("- meetingLink", "String"),
                ("- interviewerName", "String"),
                ("- preparationNotes", "String"),
                ("- outcome", "InterviewOutcome")
            ],
            "ops": [
                ("+ getId()", "Long"),
                ("+ getInterviewDate()", "LocalDateTime"),
                ("+ setOutcome(outcome)", "void")
            ]
        },
        # Architecture Controllers
        {
            "name": "ApplicationController",
            "x": 60, "y": 450, "w": 260, "h": 160,
            "attrs": [
                ("- applicationService", "ApplicationService")
            ],
            "ops": [
                ("+ getAllApplications(user)", "ResponseEntity"),
                ("+ createApplication(user, req)", "ResponseEntity"),
                ("+ updateStatus(user, id, st)", "ResponseEntity"),
                ("+ deleteApplication(user, id)", "ResponseEntity")
            ]
        },
        {
            "name": "InterviewController",
            "x": 370, "y": 450, "w": 270, "h": 160,
            "attrs": [
                ("- interviewService", "InterviewService")
            ],
            "ops": [
                ("+ getInterviews(user, appId)", "ResponseEntity"),
                ("+ scheduleInterview(user, id, req)", "ResponseEntity"),
                ("+ updateInterview(user, id, req)", "ResponseEntity")
            ]
        },
        {
            "name": "DashboardController",
            "x": 720, "y": 450, "w": 260, "h": 150,
            "attrs": [
                ("- dashboardService", "DashboardService")
            ],
            "ops": [
                ("+ getSummary(user)", "ResponseEntity"),
                ("+ getUpcomingDeadlines(user)", "ResponseEntity"),
                ("+ getStatusDistribution(user)", "ResponseEntity")
            ]
        },
        # Architecture Services
        {
            "name": "ApplicationService",
            "x": 60, "y": 680, "w": 280, "h": 180,
            "attrs": [
                ("- applicationRepository", "ApplicationRepository"),
                ("- authService", "AuthService")
            ],
            "ops": [
                ("+ getApplications(user, spec)", "Page"),
                ("+ createApplication(user, req)", "ApplicationResponse"),
                ("+ updateStatus(user, id, st)", "ApplicationResponse"),
                ("+ deleteApplication(user, id)", "void")
            ]
        },
        {
            "name": "InterviewService",
            "x": 370, "y": 680, "w": 270, "h": 170,
            "attrs": [
                ("- interviewRepository", "InterviewRepository"),
                ("- applicationRepository", "ApplicationRepository")
            ],
            "ops": [
                ("+ getInterviews(user, appId)", "List"),
                ("+ scheduleInterview(user, appId, req)", "InterviewResponse"),
                ("+ updateOutcome(user, id, outcome)", "InterviewResponse")
            ]
        },
        {
            "name": "DashboardService",
            "x": 720, "y": 680, "w": 260, "h": 160,
            "attrs": [
                ("- applicationRepository", "ApplicationRepository"),
                ("- interviewRepository", "InterviewRepository")
            ],
            "ops": [
                ("+ getSummaryStats(user)", "DashboardSummaryResponse"),
                ("+ getApproachingDeadlines(user)", "List<UpcomingDeadlineDto>"),
                ("+ getUpcomingInterviews(user)", "List<UpcomingInterviewDto>")
            ]
        }
    ]

    class_models = []
    class_views = []
    class_map = {}

    for cdata in classes_data:
        cid = gen_id()
        cvid = gen_id()
        class_map[cdata["name"]] = {"model_id": cid, "view_id": cvid, "data": cdata}

        attr_models = []
        for aname, atype in cdata["attrs"]:
            attr_models.append({
                "_type": "UMLAttribute",
                "_id": gen_id(),
                "_parent": {"$ref": cid},
                "name": aname,
                "type": atype
            })

        op_models = []
        for oname, oret in cdata["ops"]:
            op_models.append({
                "_type": "UMLOperation",
                "_id": gen_id(),
                "_parent": {"$ref": cid},
                "name": oname,
                "type": oret
            })

        class_models.append({
            "_type": "UMLClass",
            "_id": cid,
            "_parent": {"$ref": model_id},
            "name": cdata["name"],
            "attributes": attr_models,
            "operations": op_models,
            "ownedElements": []
        })

        nc = make_name_compartment(cvid, cid, cdata["name"], cdata["x"], cdata["y"], cdata["w"], 25)
        attr_comp = make_list_compartment(cvid, cid, "UMLAttributeCompartmentView", cdata["x"], cdata["y"] + 25, cdata["w"], 10)
        op_comp = make_list_compartment(cvid, cid, "UMLOperationCompartmentView", cdata["x"], cdata["y"] + 80, cdata["w"], 10)
        rec_comp = make_list_compartment(cvid, cid, "UMLReceptionCompartmentView", cdata["x"], cdata["y"] + 140, cdata["w"], 10)
        tpl_comp = make_list_compartment(cvid, cid, "UMLTemplateParameterCompartmentView", cdata["x"], cdata["y"], 0, 0)
        tpl_comp["visible"] = False

        class_views.append({
            "_type": "UMLClassView",
            "_id": cvid,
            "_parent": {"$ref": diag_id},
            "model": {"$ref": cid},
            "subViews": [nc, attr_comp, op_comp, rec_comp, tpl_comp],
            "font": "Arial;13;0",
            "left": cdata["x"],
            "top": cdata["y"],
            "width": cdata["w"],
            "height": cdata["h"],
            "nameCompartment": {"$ref": nc["_id"]},
            "attributeCompartment": {"$ref": attr_comp["_id"]},
            "operationCompartment": {"$ref": op_comp["_id"]},
            "receptionCompartment": {"$ref": rec_comp["_id"]},
            "templateParameterCompartment": {"$ref": tpl_comp["_id"]}
        })

    # Associations
    class_links = [
        ("User", "Application", "1", "0..*", "has", "290:160;370:160"),
        ("Application", "Interview", "1", "0..*", "schedules", "670:160;750:160"),
        ("ApplicationController", "ApplicationService", "1", "1", "delegates", "190:610;190:680"),
        ("InterviewController", "InterviewService", "1", "1", "delegates", "505:610;505:680"),
        ("DashboardController", "DashboardService", "1", "1", "delegates", "850:600;850:680"),
        ("ApplicationService", "InterviewService", "1", "1", "coordinates", "340:750;370:750"),
        ("InterviewService", "DashboardService", "1", "1", "aggregates", "640:750;720:750")
    ]

    assoc_views = []
    for c1, c2, m1, m2, label_text, pts in class_links:
        cm1 = class_map[c1]
        cm2 = class_map[c2]
        assoc_id = gen_id()
        assoc_vid = gen_id()
        end1_id = gen_id()
        end2_id = gen_id()

        assoc_model = {
            "_type": "UMLAssociation",
            "_id": assoc_id,
            "_parent": {"$ref": cm1["model_id"]},
            "name": label_text,
            "end1": {
                "_type": "UMLAssociationEnd",
                "_id": end1_id,
                "_parent": {"$ref": assoc_id},
                "reference": {"$ref": cm1["model_id"]},
                "multiplicity": m1
            },
            "end2": {
                "_type": "UMLAssociationEnd",
                "_id": end2_id,
                "_parent": {"$ref": assoc_id},
                "reference": {"$ref": cm2["model_id"]},
                "multiplicity": m2
            }
        }
        for cm in class_models:
            if cm["_id"] == cm1["model_id"]:
                cm["ownedElements"].append(assoc_model)
                break

        lbl_id = gen_id()
        assoc_views.append({
            "_type": "UMLAssociationView",
            "_id": assoc_vid,
            "_parent": {"$ref": diag_id},
            "model": {"$ref": assoc_id},
            "subViews": [
                {"_type": "EdgeLabelView", "_id": lbl_id, "_parent": {"$ref": assoc_vid}, "model": {"$ref": assoc_id}, "text": label_text, "visible": True, "font": "Arial;13;0", "left": 0, "top": 0, "height": 13, "alpha": 1.57, "distance": 15, "edgePosition": 1}
            ],
            "font": "Arial;13;0",
            "head": {"$ref": cm2["view_id"]},
            "tail": {"$ref": cm1["view_id"]},
            "lineStyle": 1,
            "points": pts,
            "nameLabel": {"$ref": lbl_id}
        })

    diagram = {
        "_type": "UMLClassDiagram",
        "_id": diag_id,
        "_parent": {"$ref": model_id},
        "name": "Class Diagram",
        "defaultDiagram": True,
        "ownedViews": class_views + assoc_views
    }

    model = {
        "_type": "UMLModel",
        "_id": model_id,
        "_parent": {"$ref": proj_id},
        "name": "ClassModel",
        "ownedElements": [diagram] + class_models
    }

    return {
        "_type": "Project",
        "_id": proj_id,
        "name": "Internship Application Tracker",
        "documentVersion": 1,
        "ownedElements": [model]
    }

def main():
    target_dir = r"c:\Users\moham\Downloads\JAVA-DBMS-USE CBP\uml"
    os.makedirs(target_dir, exist_ok=True)

    print("Building fully exhaustive, master-level StarUML models...")
    seq_proj = build_sequence_project()
    comp_proj = build_component_project()
    collab_proj = build_collaboration_project()
    class_proj = build_class_project()

    with open(os.path.join(target_dir, "sequence_diagram.mdj"), "w", encoding="utf-8") as f:
        json.dump(seq_proj, f, indent=2)
    print("Exhaustive sequence_diagram.mdj written.")

    with open(os.path.join(target_dir, "component_diagram.mdj"), "w", encoding="utf-8") as f:
        json.dump(comp_proj, f, indent=2)
    print("Exhaustive component_diagram.mdj written.")

    with open(os.path.join(target_dir, "collaboration_diagram.mdj"), "w", encoding="utf-8") as f:
        json.dump(collab_proj, f, indent=2)
    print("Exhaustive collaboration_diagram.mdj written.")

    with open(os.path.join(target_dir, "class_diagram.mdj"), "w", encoding="utf-8") as f:
        json.dump(class_proj, f, indent=2)
    print("Exhaustive class_diagram.mdj written.")

    # Master project
    uc_proj = json.load(open(os.path.join(target_dir, "use_case_diagram.mdj"), encoding="utf-8"))
    st_proj = json.load(open(os.path.join(target_dir, "state_diagram.mdj"), encoding="utf-8"))
    ac_proj = json.load(open(os.path.join(target_dir, "activity_diagram.mdj"), encoding="utf-8"))

    proj_id = "AAAAAAFF+h6SjaM2Hec="
    models = [
        uc_proj["ownedElements"][0],
        class_proj["ownedElements"][0],
        st_proj["ownedElements"][0],
        ac_proj["ownedElements"][0],
        seq_proj["ownedElements"][0],
        collab_proj["ownedElements"][0],
        comp_proj["ownedElements"][0]
    ]
    for m in models:
        m["_parent"] = {"$ref": proj_id}

    master = {
        "_type": "Project",
        "_id": proj_id,
        "name": "Internship Application Tracker",
        "documentVersion": 1,
        "ownedElements": models
    }

    with open(os.path.join(target_dir, "internship_tracker_all_diagrams.mdj"), "w", encoding="utf-8") as f:
        json.dump(master, f, indent=2)
    print("Exhaustive internship_tracker_all_diagrams.mdj written.")

if __name__ == "__main__":
    main()
