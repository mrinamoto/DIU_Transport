Build a complete Transport Schedule System app for DIU (Daffodil International University) using Java with OOP principles as the core architecture. Use Java for backend logic and desktop/system functionality, and use HTML, CSS, and any other necessary technologies for UI if needed. The system should be clean, modular, and realistic, with proper class design, inheritance, encapsulation, abstraction, and role-based access control.
The app should support four user roles: Student, Teacher, Staff, and Admin. Each role should have controlled permissions. Users can register, log in, and manage their profile. Admin must have secure login and full control over transport schedules, buses, drivers, employees, notifications, lost-and-found records, and transport card or billing data.
Main features required:
1. User and Admin Management
- Role-based registration and login
- Profile view and update
- Admin-only access for system management
2. Bus Information and Scheduling
- Show daily transport schedule with bus number, pickup spot, departure time, arrival time
- Route mapping with route name, stops, timing, and assigned bus
- Admin can add, edit, and delete schedules instantly
- Support special trips like exam days, club events, and industrial visits
3. Driver and Employee Information
- Show driver details like name, phone number, assigned bus, and shift
- Maintain employee records such as transport officers, helpers, and maintenance staff
4. Digital Transport Card System
- Each student can view transport card details
- Card should show card ID, validity period, and payment status
- Card status can be Active, Expired, or Pending
5. Billing and Transport Cost Information
- Display transport cost chart
- Show payment status as Paid, Due, or Partial
- No online payment gateway needed, only status tracking
6. Contact and Support
- Emergency contact page for transport manager, officer, hotline
- Feedback/contact form for users
7. Notifications and Alerts
- Show alerts for schedule updates, bus cancellations, new routes, and special trips
- Use simple database or local alert system, no push server needed
8. Lost and Found
- Users can report lost items found on buses
- Admin can manage lost-and-found entries
- Include a review or remarks section for each item
Use this project structure as the base and keep the code well organized:
project-root/
src/
  model/
    enums/
    User.java, Student.java, Teacher.java, Staff.java, Admin.java
    Bus.java, Driver.java, Employee.java, Schedule.java, Route.java
    TransportCard.java, Billing.java, Notification.java, LostFound.java, Contact.java
  services/
    UserService.java, BusService.java, DriverService.java, ScheduleService.java
    TransportCardService.java, BillingService.java, NotificationService.java, LostFoundService.java
  gui/
    DIUTransportSystem.java, LoginFrame.java, RegisterFrame.java
    UserDashboard.java, AdminDashboard.java
  util/
    Constants.java, DatabaseConnection.java, SessionManager.java, ValidationUtil.java
  storage/
    FileManager.java
diu_transport.db
Development requirements:
- Follow OOP design properly
- Use enums where necessary
- Use SQLite database connection
- Make the system beginner-friendly but professional
- Write clean, maintainable, and commented code
- Add validation, session handling, and modular service classes
- Create a simple but modern UI/dashboard
- Include sample dummy data for testing
- Make sure the project is runnable and expandable in the future
Also provide:
- ER-style data relationship planning
- class relationship overview
- suggested workflow of the app
- short explanation of why the design is good
- review the Lost & Found feature and improve it if needed for practicality
Generate the project in a structured way so I can directly start coding module by module. Also, I want the initial interface of my app to look like the image above. Convert the code to make it a beautiful app using any element including Java, Python, HTML or many more. 

diu-transport-system/
├── src/
│   ├── model/
│   │   ├── enums/
│   │   │   ├── UserRole.java
│   │   │   ├── PaymentStatus.java
│   │   │   ├── CardStatus.java
│   │   │   ├── NotificationType.java
│   │   │   ├── LostFoundStatus.java
│   │   │   ├── EmployeeRole.java
│   │   │   ├── DriverShift.java
│   │   │   ├── BusStatus.java
│   │   │   └── ContactRole.java
│   │   ├── User.java
│   │   ├── Student.java
│   │   ├── Teacher.java
│   │   ├── Staff.java
│   │   ├── Admin.java
│   │   ├── Bus.java
│   │   ├── Driver.java
│   │   ├── Employee.java
│   │   ├── Schedule.java
│   │   ├── Route.java
│   │   ├── TransportCard.java
│   │   ├── Billing.java
│   │   ├── Notification.java
│   │   ├── LostFound.java
│   │   └── Contact.java
│   ├── services/
│   │   ├── UserService.java
│   │   ├── BusService.java
│   │   ├── DriverService.java
│   │   ├── ScheduleService.java
│   │   ├── TransportCardService.java
│   │   ├── BillingService.java
│   │   ├── NotificationService.java
│   │   └── LostFoundService.java
│   ├── gui/
│   │   ├── DIUTransportSystem.java
│   │   ├── LoginFrame.java
│   │   ├── RegisterFrame.java
│   │   ├── UserDashboard.java
│   │   └── AdminDashboard.java
│   ├── util/
│   │   ├── Constants.java
│   │   ├── DatabaseConnection.java
│   │   ├── SessionManager.java
│   │   └── ValidationUtil.java
│   └── storage/
│       └── FileManager.java
├── lib/
│   └── sqlite-jdbc-3.44.1.0.jar
├── diu_transport.db
├── README.md
└── run.bat
