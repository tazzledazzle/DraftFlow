import random
import csv
from datetime import datetime, timedelta

# Set random seed for reproducibility
random.seed(42)

# Define categories with their corresponding number
CATEGORIES = {
    1: "SIDING",
    2: "METAL ROOFING",
    3: "ROOFING BUILD UP",
    4: "GUTTERS & DS",
    5: "DRIVING",
    6: "SHOP / OFFICE"
}

# Generate task names for each category
def generate_task_names():
    tasks = []

    # 1: SIDING tasks
    siding_tasks = [
        "Install vinyl siding", "Replace damaged siding panels", "Siding measurement",
        "Cut siding materials", "Clean siding surfaces", "Apply siding primer",
        "Siding insulation installation", "Siding trim work", "Caulk siding joints",
        "Siding preparation", "Siding color matching", "Install fiber cement siding",
        "Metal siding installation", "Wood siding repair", "Siding removal",
        "Siding material delivery", "Siding inspection", "Weatherproof siding corners",
        "Install siding accessories", "Siding project estimation", "Aluminum siding work",
        "Install siding vents", "Siding patch repairs", "Pressure wash siding",
        "Siding sealing", "Install cedar siding", "Siding moisture barrier installation",
        "Composite siding installation", "Complete siding maintenance", "Siding painting preparation",
        "Horizontal siding installation", "Vertical siding installation", "Custom siding cutting",
        "Install siding fasteners"
    ]

    # 2: METAL ROOFING tasks
    metal_roofing_tasks = [
        "Install metal roof sheets", "Seal metal roof joints", "Metal roof measurement",
        "Cut metal roofing panels", "Metal roof flashing installation", "Install metal roof ridge caps",
        "Metal roof inspection", "Paint metal roof sections", "Metal roofing material delivery",
        "Metal roof preparation", "Install metal roof trim", "Metal roof panel alignment",
        "Secure metal roof fasteners", "Apply metal roof sealant", "Metal roof maintenance",
        "Metal roof repair", "Clean metal roof surface", "Install metal roof underlayment",
        "Metal roof site preparation", "Metal roof project estimation", "Install metal roof vents",
        "Metal roof finishing work", "Metal roof snow guard installation", "Metal roof valley installation",
        "Metal roof edge treatment", "Metal roof extension", "Recoat metal roof",
        "Metal roof drainage setup", "Metal roof insulation", "Custom metal roof fabrication",
        "Metal roof accessory installation", "Steel roof installation", "Aluminum roof installation",
        "Copper roofing details"
    ]

    # 3: ROOFING BUILD UP tasks
    roofing_buildup_tasks = [
        "Lay roof underlayment", "Install roof flashing", "Roof deck preparation",
        "Roof insulation installation", "Apply roof coating", "Roof membrane installation",
        "Roof drainage planning", "Roof ventilation setup", "Roof material delivery",
        "Roof build-up inspection", "Install roof drip edge", "Apply roof primer",
        "Roof waterproofing", "Flat roof build-up", "Install roof ice barriers",
        "Roof vapor barrier installation", "Roof base sheet application", "Roof substrate preparation",
        "Roof build-up estimation", "Roof slope creation", "Install roof crickets",
        "Roof cap sheet installation", "Roof ply sheet application", "Install roof expansion joints",
        "Roof insulation taping", "Built-up roof maintenance", "Roof recovery board installation",
        "Roof drain collar installation", "Roof penetration flashing", "Roof deck reinforcement",
        "Install roof access hatch", "Roof curb installation", "Parapet wall flashing"
    ]

    # 4: GUTTERS & DS (Downspouts) tasks
    gutters_ds_tasks = [
        "Install aluminum gutters", "Clean gutter system", "Gutter measurement",
        "Install gutter guards", "Downspout installation", "Repair damaged gutters",
        "Gutter alignment", "Seal gutter joints", "Gutter and downspout material delivery",
        "Paint gutters and downspouts", "Install gutter extensions", "Gutter accessory installation",
        "Downspout drain work", "Install copper gutters", "Gutter system inspection",
        "Install downspout brackets", "Gutter maintenance", "Downspout redirection",
        "Gutter project estimation", "Install seamless gutters", "Gutter pitch adjustment",
        "Downspout unclogging", "Gutter section replacement", "Install gutter hangers",
        "Downspout extender installation", "Vinyl gutter installation", "Install downspout screens",
        "Gutter heat tape installation", "Metal gutter work", "Install downspout elbows",
        "Custom gutter fabrication", "Install gutter end caps", "Gutter sectional connection"
    ]

    # 5: DRIVING tasks
    driving_tasks = [
        "Deliver materials to site", "Transport equipment", "Material pickup from supplier",
        "Equipment collection", "Site-to-site transfer", "Client meeting travel",
        "Supplier visit", "Transport crew to site", "Waste disposal transportation",
        "Emergency supply delivery", "Long-distance material transport", "Heavy equipment transport",
        "Specialized tool delivery", "Return rental equipment", "Fuel supply run",
        "Deliver completed project documentation", "Site survey travel", "Equipment maintenance transport",
        "Material sample collection", "Pre-bid site visit", "Permit acquisition travel",
        "Building department visit", "Transport safety equipment", "Deliver project signs",
        "Plan delivery to client", "Transport portable facilities", "Transport site office equipment",
        "Client site inspection transfer", "Municipal office travel", "Insurance inspection transport",
        "Transport office supplies", "Maintenance supply delivery", "Specialty material transport"
    ]

    # 6: SHOP / OFFICE tasks
    shop_office_tasks = [
        "Inventory supplies", "Process order forms", "Order materials",
        "Schedule deliveries", "Update project timelines", "Prepare invoices",
        "Client phone consultation", "Equipment maintenance", "Tool organization",
        "Equipment inspection", "Sharpen tools", "Clean shop area",
        "Organize storage space", "Safety equipment inspection", "Office filing",
        "Project documentation", "Client proposal preparation", "Budget review",
        "Team scheduling", "Client email correspondence", "Vendor coordination",
        "Training session", "Payroll processing", "Project cost analysis",
        "Update safety protocols", "Equipment repair", "Material return processing",
        "Project planning", "Software update", "Contract preparation",
        "Equipment calibration", "Office supply inventory", "Project closeout documentation",
        "Process timesheets"
    ]

    # Add category number to each task name and compile into one list
    task_id = 1
    for category_id, task_list in [
        (1, siding_tasks),
        (2, metal_roofing_tasks),
        (3, roofing_buildup_tasks),
        (4, gutters_ds_tasks),
        (5, driving_tasks),
        (6, shop_office_tasks)
    ]:
        for task_name in task_list:
            # Format: task_id, task_name, category_id, category_name, estimated_hours
            # Estimated hours will be random between 1.0 and 10.0
            estimated_hours = round(random.uniform(1.0, 10.0), 1)
            tasks.append((
                task_id,
                task_name,
                category_id,
                CATEGORIES[category_id],
                estimated_hours
            ))
            task_id += 1
            if task_id > 200:
                break

        if task_id > 200:
            break

    # Ensure we have exactly 200 tasks
    tasks = tasks[:200]
    return tasks

# Generate timesheet entries
def generate_timesheet_entries(tasks, num_entries=1600):
    entries = []

    # Create a list of employee names
    employee_names = [
        "John Smith", "Emma Johnson", "Michael Chen", "Sarah Davis",
        "David Rodriguez", "Lisa Kim", "Robert Taylor", "Maria Garcia",
        "James Wilson", "Jennifer Brown", "Thomas Lee", "Jessica Martinez",
        "William Anderson", "Elizabeth Thomas", "Christopher Jackson", "Sophia Clark"
    ]

    # Generate start date (30 days ago)
    start_date = datetime.now() - timedelta(days=30)

    # Generate entries
    entry_id = 1
    for i in range(num_entries):
        # Select a random task
        task = random.choice(tasks)
        task_id, task_name, category_id, category_name, estimated_hours = task

        # Generate a random date within the last 30 days
        random_days = random.randint(0, 29)
        work_date = start_date + timedelta(days=random_days)
        work_date_str = work_date.strftime("%Y-%m-%d")

        # Generate random hours worked (between 1.0 and estimated hours)
        hours_worked = round(random.uniform(1.0, min(estimated_hours, 8.0)), 1)

        # Select a random employee
        employee_name = random.choice(employee_names)

        # Add optional notes
        notes = ""
        if random.random() < 0.3:  # 30% chance of having notes
            notes_options = [
                "Completed successfully",
                "Requires follow-up",
                "Material shortage delayed work",
                "Weather conditions affected progress",
                "Additional time needed for completion",
                "Client requested modifications",
                "Waiting on permits",
                "Collaboration with other team required",
                "Equipment malfunction delayed work",
                "Ahead of schedule"
            ]
            notes = random.choice(notes_options)

        # Create entry
        entry = (
            entry_id,
            task_id,
            task_name,
            category_id,
            category_name,
            employee_name,
            work_date_str,
            hours_worked,
            estimated_hours,
            notes
        )
        entries.append(entry)
        entry_id += 1

    return entries

# Main execution
def main():
    # Generate 200 tasks
    tasks = generate_task_names()

    # Generate 1600 timesheet entries
    entries = generate_timesheet_entries(tasks, 1600)

    # Write tasks to CSV
    with open('tasks.csv', 'w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(["task_id", "task_name", "category_id", "category_name", "estimated_hours"])
        writer.writerows([(t[0], t[1], t[2], t[3], t[4]) for t in tasks])

    # Write entries to CSV
    with open('timesheet_entries.csv', 'w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow([
            "entry_id", "task_id", "task_name", "category_id", "category_name",
            "employee_name", "work_date", "hours_worked", "estimated_hours", "notes"
        ])
        writer.writerows(entries)

    print(f"Generated {len(tasks)} tasks and {len(entries)} timesheet entries")
    print(f"Data saved to 'tasks.csv' and 'timesheet_entries.csv'")

if __name__ == "__main__":
    main()