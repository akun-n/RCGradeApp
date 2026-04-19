package com.rcgrade.app.views;
 
import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * MainView — the single-page UI for RC GradeNavigator.
 *
 * Robert College grades a course with five components:
 *   W1, W2  — written (mid-term) exams
 *   P1, P2  — performance (oral/project) exams
 *   P5      — automatically set to the BEST of {W1, W2, P1, P2}
 *
 * Official final course average = (W1 + W2 + P1 + P2 + P5) / 5
 * This effectively gives the strongest exam double weight.
 *
 * Passing threshold : 60
 * Honors            : 70
 * High Honors       : 85
 *
 * Weighted GPA = Σ(courseAverage_i × hoursPerWeek_i) / Σ(hoursPerWeek_i)
 * AP courses are NOT weighted extra per the RC Student Handbook.
 */
@Route("")
@PageTitle("RC GradeNavigator")
public class MainView extends VerticalLayout 
{
 
    /**
     * Application State
     * All added course rows; the list is the single source of truth for the table, the GPA card, and the GPA Planner.
     */
    private final List<GradeRow> classRows = new ArrayList<>();
    private final String[] selectedCourse = {""};
 
    // GPA display (header)
    // These three elements are updated by updateGpaDisplay() every time a course is added or removed.
    private Span gpaValueSpan;
    private Span gpaStatusSpan;
    private Div gpaCardDiv;
 
    // Add-course panel 
    private List<Button> courseButtons;
    private TextField customClassField;
    private NumberField addHoursField;
    private NumberField addW1;
    private NumberField addW2;
    private NumberField addP1;
    private NumberField addP2;
    private NumberField addP5;
    private NumberField addAvg;
    private Span addResultMsg;
 
    // Course table
    private Grid<GradeRow> classTable;
    private Div tableSection;
 
    // Predictor panel
    private NumberField predW1, predW2, predP1, predP2, predTarget;
    private Div predResultDiv;
 
    // GPA-planner panel
    private NumberField planTargetGpa;
    private NumberField planAdditionalHours;
    private Div planResultDiv;
 
    //  Constructor
    public MainView() 
    {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "#f0f4f9").set("min-height", "100vh");
 
        add(buildHeader());
 
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setMaxWidth("1180px");
        content.setPadding(true);
        content.setSpacing(true);
        content.getStyle().set("margin", "0 auto").set("box-sizing", "border-box");
 
        // Row 1 : Add Course  +  Grade Predictor
        HorizontalLayout topRow = new HorizontalLayout(buildAddCoursePanel(), buildPredictorPanel());
        topRow.setWidthFull();
        topRow.setSpacing(true);
        topRow.setAlignItems(FlexComponent.Alignment.START);
        topRow.getStyle().set("flex-wrap", "wrap").set("gap", "20px");
 
        // Row 2 : Course Table
        tableSection = buildTableSection();
 
        // Row 3 : GPA Planner
        Component plannerPanel = buildGpaPlannerPanel();
 
        content.add(topRow, tableSection, plannerPanel);
        add(content);
    }
 
    /**  
     * Header
     *
     * Builds the full-width gradient header containing:
     *  • Left  — app name and tagline
     *  • Right — live weighted-GPA card whose background tint reflects the tier
     */
    private Component buildHeader() 
    {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle()
                .set("background", "linear-gradient(135deg, #700e0e 0%, #1a3a8f 100%)")
                .set("box-shadow", "0 3px 12px rgba(0,0,0,0.25)")
                .set("padding", "16px 28px");
 
        // Left – branding
        Span appName = new Span("RC GradeNavigator");
        appName.getStyle()
                .set("color", "white")
                .set("font-size", "1.55rem")
                .set("font-weight", "800")
                .set("letter-spacing", "-0.4px")
                .set("font-family", "'Georgia', serif");
 
        Span tagline = new Span("Robert College Academic Planner");
        tagline.getStyle()
                .set("color", "rgba(255,255,255,0.6)")
                .set("font-size", "0.78rem")
                .set("display", "block")
                .set("letter-spacing", "0.3px");
 
        Div titleDiv = new Div(appName, tagline);
        titleDiv.getStyle().set("flex", "1");
 
        // Right – GPA card
        gpaCardDiv = new Div();
        gpaCardDiv.getStyle()
                .set("background", "rgba(255,255,255,0.12)")
                .set("border", "1px solid rgba(255,255,255,0.22)")
                .set("border-radius", "14px")
                .set("padding", "10px 22px")
                .set("text-align", "center")
                .set("min-width", "165px")
                .set("backdrop-filter", "blur(6px)")
                .set("transition", "background 0.4s");
 
        Span gpaLabel = new Span("WEIGHTED GPA");
        gpaLabel.getStyle()
                .set("color", "rgba(255,255,255,0.65)")
                .set("font-size", "0.65rem")
                .set("display", "block")
                .set("letter-spacing", "1.2px")
                .set("margin-bottom", "2px");
 
        gpaValueSpan = new Span("—");
        gpaValueSpan.getStyle()
                .set("color", "white")
                .set("font-size", "2.4rem")
                .set("font-weight", "800")
                .set("display", "block")
                .set("line-height", "1.0")
                .set("font-family", "'Georgia', serif");
 
        gpaStatusSpan = new Span("No courses added");
        gpaStatusSpan.getStyle()
                .set("font-size", "0.7rem")
                .set("color", "rgba(255,255,255,0.6)")
                .set("display", "block")
                .set("margin-top", "2px");
 
        gpaCardDiv.add(gpaLabel, gpaValueSpan, gpaStatusSpan);
        header.add(titleDiv, gpaCardDiv);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return header;
    }
 
    /**  
     * Add-Course Panel
     *
     * Builds the "Add Course" card.
     *
     * Student flow:
     *  1. Pick a course chip (or type a custom name via "Other").
     *  2. Enter credit hours / week (used for GPA weighting).
     *  3. Optionally enter any grades already received (blank = not yet taken).
     *  4. P5 (best grade) and Average update live as grades are typed.
     *     Average formula: (sum of entered + P5) / (count + 1)
     *     → equivalent to (W1+W2+P1+P2+P5)/5 when all four grades are present.
     *  5. "Add Course" appends a GradeRow and refreshes the GPA card.
     */
    private Component buildAddCoursePanel() 
    {
        VerticalLayout panel = card();
        panel.getStyle().set("flex", "1").set("min-width", "340px");
 
        // Title
        H3 title = new H3("Add Course");
        title.getStyle().set("margin", "0 0 18px 0").set("color", "#0d1b4b").set("font-size", "1.1rem")
                .set("font-family", "'Georgia', serif").set("border-bottom", "2px solid #e8edf5")
                .set("padding-bottom", "10px");
 
        // Course catalog chips
        Span catLabel = sectionLabel("Course");
        HorizontalLayout catalogRow = new HorizontalLayout();
        catalogRow.getStyle().set("flex-wrap", "wrap").set("gap", "6px");
        catalogRow.setPadding(false);
        catalogRow.setSpacing(false);
 
        courseButtons = new ArrayList<>();
        List<String> courses = List.of("Mathematics", "Physics", "Chemistry", "Biology", "History", "Turkish", "English", "Computer Science", "Other");
        for (String name : courses) 
        {
            Button btn = new Button(name);
            chipStyle(btn, false);
            btn.addClickListener(e -> {
                courseButtons.forEach(b -> chipStyle(b, false));
                chipStyle(btn, true);
                selectedCourse[0] = name;
                customClassField.setVisible("Other".equals(name));
                if (!customClassField.isVisible()) 
                {
                    customClassField.clear();
                }
            });
            courseButtons.add(btn);
            catalogRow.add(btn);
        }
 
        customClassField = new TextField();
        customClassField.setPlaceholder("Custom course name…");
        customClassField.setWidthFull();
        customClassField.setVisible(false);
        customClassField.getStyle().set("margin-top", "6px");
 
        // Hours input
        addHoursField = new NumberField("Hours / Week");
        addHoursField.setMin(0);
        addHoursField.setMax(20);
        addHoursField.setStep(1);
        addHoursField.setValue(0.0);
        addHoursField.setWidthFull();
        addHoursField.getStyle().set("margin-top", "4px");
 
        // Grade inputs (optional)
        Span gradesLabel = sectionLabel("Grades  ·  leave blank if not yet taken");
 
        addW1 = optGradeField("W1");
        addW2 = optGradeField("W2");
        addP1 = optGradeField("P1");
        addP2 = optGradeField("P2");
 
        HorizontalLayout gr1 = gradeRow(addW1, addW2);
        HorizontalLayout gr2 = gradeRow(addP1, addP2);
 
        addP5 = new NumberField("P5");
        addP5.setReadOnly(true);
        addP5.setWidthFull();
        addAvg = new NumberField("Average");
        addAvg.setReadOnly(true);
        addAvg.setWidthFull();
        HorizontalLayout calcRow = gradeRow(addP5, addAvg);
 
        // Live recalc
        Runnable recalc = () -> {
            List<Double> entered = enteredValues(addW1, addW2, addP1, addP2);
            if (entered.isEmpty()) 
            {
                addP5.setValue(0.0);
                addAvg.setValue(0.0);
            } 
            else 
            {
                addP5.setValue(round2(entered.stream().mapToDouble(Double::doubleValue).max().orElse(0)));
                addAvg.setValue(round2(entered.stream().mapToDouble(Double::doubleValue).average().orElse(0)));
            }
        };
        addW1.addValueChangeListener(e -> recalc.run());
        addW2.addValueChangeListener(e -> recalc.run());
        addP1.addValueChangeListener(e -> recalc.run());
        addP2.addValueChangeListener(e -> recalc.run());
 
        addResultMsg = new Span();
        addResultMsg.getStyle().set("font-size", "0.8rem").set("min-height", "18px");
 
        Button addBtn = primaryBtn("＋  Add Course");
        addBtn.addClickListener(e -> handleAddCourse());
 
        panel.add(title, catLabel, catalogRow, customClassField, addHoursField,
                gradesLabel, gr1, gr2, calcRow, addResultMsg, addBtn);
        return panel;
    }
    
    /**
     * Grade Predictor Panel
     * 
     * Builds the "Grade Predictor" card.
     *
     * The student enters the subset of {W1,W2,P1,P2} they already have; blank
     * fields mean that assessment hasn't happened yet.  Clicking Calculate
     * reverse-solves for the minimum score `x` the student needs on every
     * remaining assessment to hit their target, using the FULL RC formula:
     *
     *   final = (W1 + W2 + P1 + P2 + P5) / 5,   P5 = max(W1, W2, P1, P2)
     *
     * Because P5 depends on `x` itself, the solution branches into two cases
     * (see handlePredict for the derivation):
     *   Case A — x ≥ currentBest  →  x = (5·target − knownSum) / (missing + 1)
     *   Case B — x < currentBest  →  x = (5·target − knownSum − currentBest) / missing
     */
    private Component buildPredictorPanel() 
    {
        VerticalLayout panel = card();
        panel.getStyle().set("flex", "1").set("min-width", "340px");
 
        H3 title = new H3("Grade Predictor");
        title.getStyle().set("margin", "0 0 6px 0").set("color", "#0d1b4b").set("font-size", "1.1rem")
                .set("font-family", "'Georgia', serif").set("border-bottom", "2px solid #e8edf5")
                .set("padding-bottom", "10px");
 
        Span desc = new Span("Enter the grades you already have and your target average — we'll calculate the minimum you need on the rest.");
        desc.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "#6b7280")
                .set("display", "block")
                .set("margin-bottom", "16px")
                .set("line-height", "1.5");
 
        Span inputLabel = sectionLabel("Current grades  ·  leave blank = not yet taken");
 
        predW1 = optGradeField("W1");
        predW2 = optGradeField("W2");
        predP1 = optGradeField("P1");
        predP2 = optGradeField("P2");
 
        HorizontalLayout pr1 = gradeRow(predW1, predW2);
        HorizontalLayout pr2 = gradeRow(predP1, predP2);
 
        predTarget = new NumberField("Target Average");
        predTarget.setMin(0);
        predTarget.setMax(100);
        predTarget.setStep(0.1);
        predTarget.setPlaceholder("e.g. 85");
        predTarget.setWidthFull();
        predTarget.getStyle().set("margin-top", "4px");
 
        predResultDiv = new Div();
        predResultDiv.setWidthFull();
 
        Button calcBtn = primaryBtn("Calculate");
        calcBtn.addClickListener(e -> handlePredict());
 
        // Grading scale reference
        Div scaleRef = buildScaleReference();
 
        panel.add(title, desc, inputLabel, pr1, pr2, predTarget, calcBtn, predResultDiv, scaleRef);
        return panel;
    }
 
    //  Course Table Section
    private Div buildTableSection() 
    {
        Div section = new Div();
        section.setWidthFull();
        section.setVisible(false);
 
        classTable = new Grid<>(GradeRow.class, false);
        classTable.setWidthFull();
        classTable.getStyle().set("border-radius", "8px").set("overflow", "hidden");
 
        classTable.addColumn(GradeRow::getClassName)
                .setHeader("Course").setFlexGrow(3).setSortable(true);
        classTable.addColumn(r -> String.format("%.0f h", r.getClassHours()))
                .setHeader("Hrs/Wk").setFlexGrow(0).setWidth("80px");
        classTable.addColumn(r -> fmtGrade(r.getW1()))
                .setHeader("W1").setFlexGrow(1);
        classTable.addColumn(r -> fmtGrade(r.getW2()))
                .setHeader("W2").setFlexGrow(1);
        classTable.addColumn(r -> fmtGrade(r.getP1()))
                .setHeader("P1").setFlexGrow(1);
        classTable.addColumn(r -> fmtGrade(r.getP2()))
                .setHeader("P2").setFlexGrow(1);
        classTable.addColumn(r -> r.isAnyEntered() ? String.format("%.1f", r.getP5()) : "—")
                .setHeader("P5 ★").setFlexGrow(1);
        classTable.addColumn(r -> r.isAnyEntered() ? String.format("%.2f", r.getAverage()) : "—")
                .setHeader("Average").setFlexGrow(1).setSortable(true);
 
        classTable.addComponentColumn(row -> {
            Button del = new Button("✕");
            del.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            del.addClickListener(e -> {
                classRows.remove(row);
                classTable.setItems(classRows);
                if (classRows.isEmpty()) section.setVisible(false);
                updateGpaDisplay();
            });
            return del;
        }).setHeader("").setFlexGrow(0).setWidth("56px");
 
        Div tableCard = new Div();
        tableCard.setWidthFull();
        tableCard.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 1px 6px rgba(0,0,0,0.07)")
                .set("padding", "20px");
 
        H3 tableTitle = new H3("My Courses");
        tableTitle.getStyle().set("margin", "0 0 14px 0").set("color", "#0d1b4b").set("font-size", "1.1rem")
                .set("font-family", "'Georgia', serif").set("border-bottom", "2px solid #e8edf5")
                .set("padding-bottom", "10px");
 
        tableCard.add(tableTitle, classTable);
        section.add(tableCard);
        return section;
    }
 
    
    /** 
     * GPA Planner Panel
     * 
     * Builds the semester-level "GPA Planner" card.
     *
     * Reads the current weighted GPA from graded courses in the table, then
     * asks the student two inputs:
     *   • Target weighted GPA they want to achieve by year-end
     *   • Total credit hours / week of courses not yet added to the table
     *
     * Derived formula:
     *   targetGPA = (currentWeightedSum + neededAvg × remainingHours) / totalHours
     *   → neededAvg = (targetGPA × totalHours − currentWeightedSum) / remainingHours
     *
     * Only courses that have at least one grade entered contribute to
     * currentWeightedSum; ungraded courses are excluded as their average
     * is still undefined.
     */
    private Component buildGpaPlannerPanel() 
    {
        VerticalLayout panel = card();
 
        H3 title = new H3("GPA Planner");
        title.getStyle().set("margin", "0 0 6px 0").set("color", "#0d1b4b").set("font-size", "1.1rem")
                .set("font-family", "'Georgia', serif").set("border-bottom", "2px solid #e8edf5")
                .set("padding-bottom", "10px");
 
        Span desc = new Span(
                "Set a target weighted GPA and enter the total hours/week of courses you still have to complete. " +
                "We'll tell you the minimum average you need across those remaining courses.");
        desc.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "#6b7280")
                .set("display", "block")
                .set("margin-bottom", "16px")
                .set("line-height", "1.5");
 
        HorizontalLayout inputRow = new HorizontalLayout();
        inputRow.setWidthFull();
        inputRow.setSpacing(true);
 
        planTargetGpa = new NumberField("Target Weighted GPA");
        planTargetGpa.setMin(0);
        planTargetGpa.setMax(100);
        planTargetGpa.setStep(0.1);
        planTargetGpa.setPlaceholder("e.g. 80.0");
        planTargetGpa.setWidthFull();
 
        planAdditionalHours = new NumberField("Remaining Course Hours / Week");
        planAdditionalHours.setMin(0);
        planAdditionalHours.setStep(1);
        planAdditionalHours.setPlaceholder("e.g. 14");
        planAdditionalHours.setWidthFull();
 
        inputRow.add(planTargetGpa, planAdditionalHours);
 
        planResultDiv = new Div();
        planResultDiv.setWidthFull();
 
        Button planBtn = primaryBtn("Calculate");
        planBtn.addClickListener(e -> handlePlannerCalc());
 
        panel.add(title, desc, inputRow, planBtn, planResultDiv);
        return panel;
    }
 
    /**
     * Event Handlers 
     * Validates the Add-Course form, constructs a GradeRow, appends it to the
     * shared list, refreshes the table, and triggers a GPA recalculation.
     * Resets all grade inputs after a successful add so the panel is immediately
     * ready for the next course entry.
     */
    private void handleAddCourse() 
    {
        if (selectedCourse[0].isEmpty()) 
        {
            addResultMsg.getStyle().set("color", "#dc2626");
            addResultMsg.setText("⚠  Please select a course.");
            return;
        }
        String name = selectedCourse[0];
        if ("Other".equals(name)) 
        {
            name = customClassField.getValue().trim();
            if (name.isEmpty()) 
            {
                addResultMsg.getStyle().set("color", "#dc2626");
                addResultMsg.setText("⚠  Please enter a custom course name.");
                return;
            }
        }
 
        double hours = orZero(addHoursField.getValue());
        Double w1val = addW1.getValue();
        Double w2val = addW2.getValue();
        Double p1val = addP1.getValue();
        Double p2val = addP2.getValue();
 
        List<Double> entered = enteredValues(addW1, addW2, addP1, addP2);
        double p5  = entered.isEmpty() ? 0 : entered.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double avg = entered.isEmpty() ? 0 : entered.stream().mapToDouble(Double::doubleValue).average().orElse(0);
 
        classRows.add(new GradeRow(name, hours, w1val, w2val, p1val, p2val, p5, avg));
        classTable.setItems(classRows);
        tableSection.setVisible(true);
        updateGpaDisplay();
 
        addResultMsg.getStyle().set("color", "#16a34a");
        addResultMsg.setText("✓  " + name + " added.");
 
        // Reset fields
        addW1.clear(); 
        addW2.clear(); 
        addP1.clear(); 
        addP2.clear();
        addP5.setValue(0.0); 
        addAvg.setValue(0.0);
    }
 
    private void handlePredict() 
    {
        Double w1v = predW1.getValue();
        Double w2v = predW2.getValue();
        Double p1v = predP1.getValue();
        Double p2v = predP2.getValue();
        Double target = predTarget.getValue();
 
        if (target == null) 
        {
            renderResult(predResultDiv, "error", "Please enter a target average.");
            return;
        }
        if (target < 0 || target > 100) 
        {
            renderResult(predResultDiv, "error", "Target must be between 0 and 100.");
            return;
        }
 
        List<Double> entered = enteredValues(predW1, predW2, predP1, predP2);
        int enteredCount = entered.size();
        int missingCount = 4 - enteredCount;
 
        if (missingCount == 0) 
        {
            double currentAvg = entered.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            if (currentAvg >= target)
                renderResult(predResultDiv, "success",
                        String.format("All grades are in — your average is %.2f. Target of %.1f ✓", currentAvg, target));
            else
                renderResult(predResultDiv, "warning",
                        String.format("All grades are in. Your average (%.2f) is below your target (%.1f). No more assessments remain.", currentAvg, target));
            return;
        }
 
        if (enteredCount == 0)
        {
            renderResult(predResultDiv, "info",
                    String.format("No grades entered yet.\nYou need %.1f on all 4 assessments (W1, W2, P1, P2) to reach %.1f.", target, target));
            return;
        }
 
        double sumEntered = entered.stream().mapToDouble(Double::doubleValue).sum();
        double needed     = (target * 4.0 - sumEntered) / missingCount;
 
        List<String> missingNames = new ArrayList<>();
        if (w1v == null) missingNames.add("W1");
        if (w2v == null) missingNames.add("W2");
        if (p1v == null) missingNames.add("P1");
        if (p2v == null) missingNames.add("P2");
        String missing = String.join(", ", missingNames);
 
        if (needed > 100) 
        {
            renderResult(predResultDiv, "error",
                    String.format(
                        "Target %.1f is not achievable with your current grades.\n" +
                        "You would need %.1f on %s — above the 100-point maximum.\n" +
                        "Consider revising your target.",
                        target, needed, missing));
        } 
        else if (needed < 0) 
        {
            renderResult(predResultDiv, "success",
                    String.format(
                        "You've already secured your target!\n" +
                        "Even scoring 0 on %s, your average will exceed %.1f. 🎉",
                        missing, target));
        } 
        else 
        {
            String difficulty = needed >= 85 ? "🔥 Challenging" : needed >= 70 ? "📘 Attainable" : "✅ Comfortable";
            String tier = gradeTier(needed);
            renderResult(predResultDiv, "result",
                    String.format(
                        "You need at least  %.1f  on:  %s\n\n" +
                        "Difficulty: %s\n" +
                        "That score qualifies as: %s\n" +
                        "Target average: %.1f",
                        needed, missing, difficulty, tier, target));
        }
    }
 
    private void handlePlannerCalc() 
    {
        Double targetGpa = planTargetGpa.getValue();
        Double additionalHours = planAdditionalHours.getValue();
 
        if (targetGpa == null || additionalHours == null) 
        {
            renderResult(planResultDiv, "error", "Please fill in both the target GPA and remaining course hours.");
            return;
        }
        if (additionalHours <= 0) 
        {
            renderResult(planResultDiv, "error", "Remaining course hours must be greater than 0.");
            return;
        }
        if (targetGpa < 0 || targetGpa > 100) 
        {
            renderResult(planResultDiv, "error", "Target GPA must be between 0 and 100.");
            return;
        }
 
        double currentWeightedSum = 0, currentHours = 0;
        for (GradeRow r : classRows) 
        {
            if (r.isAnyEntered()) 
            {
                currentWeightedSum += r.getAverage() * r.getClassHours();
                currentHours       += r.getClassHours();
            }
        }
 
        double totalHours = currentHours + additionalHours;
        double needed = (targetGpa * totalHours - currentWeightedSum) / additionalHours;
 
        String currentStr = currentHours > 0
                ? String.format("Current weighted GPA:  %.2f  (based on %.0f hrs/wk)", currentWeightedSum / currentHours, currentHours)
                : "No graded courses recorded yet — current GPA treated as 0.";
 
        if (needed > 100) 
        {
            renderResult(planResultDiv, "error",
                    String.format(
                        "%s\n\n" +
                        "A target GPA of %.1f with %.0f remaining hrs/wk is not reachable.\n" +
                        "You would need %.1f per course (max is 100).\n" +
                        "Try a lower target or add more hours.",
                        currentStr, targetGpa, additionalHours, needed));
        } 
        else if (needed < 0) 
        {
            renderResult(planResultDiv, "success",
                    String.format(
                        "%s\n\n" +
                        "Your current GPA already guarantees a result above %.1f — " +
                        "no matter how the remaining courses go. 🎉",
                        currentStr, targetGpa));
        } 
        else 
        {
            String tier = gradeTier(needed);
            String diff = needed >= 85 ? "🔥 Challenging" : needed >= 70 ? "📘 Attainable" : "✅ Comfortable";
            renderResult(planResultDiv, "result",
                    String.format(
                        "%s\n\n" +
                        "To reach GPA  %.1f  you need an average of:\n\n" +
                        "    ▶  %.1f  across your remaining %.0f hrs/wk of courses\n\n" +
                        "That level qualifies as: %s\n" +
                        "Difficulty: %s",
                        currentStr, targetGpa, needed, additionalHours, tier, diff));
        }
    }
 
    //  GPA Display Update
    private void updateGpaDisplay() 
    {
        double totalWeighted = 0, totalHours = 0;
        for (GradeRow r : classRows) 
        {
            if (r.isAnyEntered()) 
            {
                totalWeighted += r.getAverage() * r.getClassHours();
                totalHours    += r.getClassHours();
            }
        }
 
        if (totalHours == 0) 
        {
            gpaValueSpan.setText("—");
            gpaStatusSpan.setText("No grades entered");
            gpaCardDiv.getStyle().set("background", "rgba(255,255,255,0.12)");
            return;
        }
 
        double gpa = totalWeighted / totalHours;
        gpaValueSpan.setText(String.format("%.2f", gpa));
 
        if (gpa >= 85) 
        {
            gpaStatusSpan.setText("✦ High Honors");
            gpaCardDiv.getStyle().set("background", "rgba(234,179,8,0.30)");
        } 
        else if (gpa >= 70) 
        {
            gpaStatusSpan.setText("✦ Honors");
            gpaCardDiv.getStyle().set("background", "rgba(59,130,246,0.30)");
        } 
        else if (gpa >= 60) 
        {
            gpaStatusSpan.setText("Pass");
            gpaCardDiv.getStyle().set("background", "rgba(34,197,94,0.20)");
        } 
        else 
        {
            gpaStatusSpan.setText("⚠ Below Passing");
            gpaCardDiv.getStyle().set("background", "rgba(239,68,68,0.30)");
        }
    }
 
    /**
     * UI Helpers 
     * 
     * Renders a styled result box inside the given container div.
     * Any previous content is cleared first so repeated clicks do not stack
     * multiple result boxes.
     *
     * @param container  the Div to render into (predResultDiv or planResultDiv)
     * @param type       "success" | "error" | "warning" | "info" | "result"
     * @param message    text content; \n characters produce visible line breaks
     *                   (white-space: pre-line is set on the box)
     */
    private void renderResult(Div container, String type, String message) 
    {
        container.removeAll();
        Div box = new Div();
        box.getStyle()
                .set("border-radius", "10px")
                .set("padding", "14px 18px")
                .set("margin-top", "14px")
                .set("font-size", "0.85rem")
                .set("white-space", "pre-line")
                .set("line-height", "1.6");
 
        switch (type) 
        {
            case "success" -> box.getStyle()
                    .set("background", "#f0fdf4").set("color", "#14532d")
                    .set("border-left", "4px solid #22c55e");
            case "error"   -> box.getStyle()
                    .set("background", "#fef2f2").set("color", "#7f1d1d")
                    .set("border-left", "4px solid #ef4444");
            case "warning" -> box.getStyle()
                    .set("background", "#fffbeb").set("color", "#78350f")
                    .set("border-left", "4px solid #f59e0b");
            case "info"    -> box.getStyle()
                    .set("background", "#eff6ff").set("color", "#1e3a5f")
                    .set("border-left", "4px solid #3b82f6");
            case "result"  -> box.getStyle()
                    .set("background", "#f5f3ff").set("color", "#2e1065")
                    .set("border-left", "4px solid #7c3aed")
                    .set("font-weight", "500");
            default        -> box.getStyle()
                    .set("background", "#f9fafb").set("color", "#374151");
        }
 
        box.add(new Span(message));
        container.add(box);
    }
 
    private Div buildScaleReference() 
    {
        Div div = new Div();
        div.getStyle()
                .set("margin-top", "18px")
                .set("border-top", "1px solid #f0f0f0")
                .set("padding-top", "14px");
 
        Span scaleTitle = new Span("RC Grading Scale");
        scaleTitle.getStyle()
                .set("font-size", "0.7rem")
                .set("font-weight", "700")
                .set("color", "#9ca3af")
                .set("display", "block")
                .set("margin-bottom", "8px")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.8px");
 
        HorizontalLayout badges = new HorizontalLayout();
        badges.setPadding(false);
        badges.setSpacing(false);
        badges.getStyle().set("flex-wrap", "wrap").set("gap", "6px");
 
        badges.add(badge("≥ 85 — High Honors", "#92400e", "#fffbeb"));
        badges.add(badge("≥ 70 — Honors", "#1e3a5f", "#eff6ff"));
        badges.add(badge("≥ 60 — Pass", "#14532d", "#f0fdf4"));
        badges.add(badge("< 60 — Fail", "#7f1d1d", "#fef2f2"));
 
        div.add(scaleTitle, badges);
        return div;
    }
 
    private Div badge(String text, String color, String bg) 
    {
        Div b = new Div(new Span(text));
        b.getStyle()
                .set("background", bg)
                .set("color", color)
                .set("border-radius", "6px")
                .set("padding", "3px 9px")
                .set("font-size", "0.72rem")
                .set("font-weight", "600");
        return b;
    }
 
    private Span sectionLabel(String text) 
    {
        Span s = new Span(text);
        s.getStyle()
                .set("font-size", "0.75rem")
                .set("font-weight", "700")
                .set("color", "#6b7280")
                .set("display", "block")
                .set("margin-top", "14px")
                .set("margin-bottom", "6px")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.5px");
        return s;
    }
 
    private VerticalLayout card() 
    {
        VerticalLayout c = new VerticalLayout();
        c.getStyle()
                .set("background", "white")
                .set("border-radius", "14px")
                .set("box-shadow", "0 1px 6px rgba(0,0,0,0.07), 0 4px 16px rgba(0,0,0,0.04)")
                .set("padding", "22px");
        c.setSpacing(false);
        c.setPadding(false);
        return c;
    }
 
    private NumberField optGradeField(String label) 
    {
        NumberField f = new NumberField(label);
        f.setStep(0.1);
        f.setMin(0);
        f.setMax(100);
        f.setPlaceholder("—");
        f.setClearButtonVisible(true);
        f.setWidthFull();
        return f;
    }
 
    private HorizontalLayout gradeRow(NumberField a, NumberField b) 
    {
        HorizontalLayout row = new HorizontalLayout(a, b);
        row.setWidthFull();
        row.setSpacing(true);
        return row;
    }
 
    private Button primaryBtn(String label) 
    {
        Button btn = new Button(label);
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.setWidthFull();
        btn.getStyle().set("margin-top", "14px").set("border-radius", "8px").set("font-weight", "600");
        return btn;
    }
 
    private void chipStyle(Button btn, boolean selected) 
    {
        if (selected) 
        {
            btn.getStyle()
                    .set("background", "#0d1b4b").set("color", "white")
                    .set("border", "none").set("border-radius", "20px")
                    .set("font-size", "0.8rem").set("padding", "4px 14px")
                    .set("cursor", "pointer").set("font-weight", "600");
        } 
        else 
        {
            btn.getStyle()
                    .set("background", "#f1f5f9").set("color", "#374151")
                    .set("border", "1px solid #d1d5db").set("border-radius", "20px")
                    .set("font-size", "0.8rem").set("padding", "4px 14px")
                    .set("cursor", "pointer").set("font-weight", "500");
        }
    }
 
    //  Utility
    private List<Double> enteredValues(NumberField... fields) 
    {
        List<Double> list = new ArrayList<>();
        for (NumberField f : fields) 
        {
            if (f.getValue() != null) 
            {
                list.add(f.getValue());
            }
        }
        return list;
    }
 
    private double orZero(Double v)     { return v == null ? 0.0 : v; }
    private double round2(double v)     { return Math.round(v * 100.0) / 100.0; }
    private String fmtGrade(Double v)   { return v == null ? "—" : String.format("%.1f", v); }
 
    private String gradeTier(double score) 
    {
        if (score >= 85) 
        {
            return "High Honors (≥ 85)";
        }
        if (score >= 70) 
        {
            return "Honors (≥ 70)";
        }
        if (score >= 60) 
        {
            return "Pass (≥ 60)";
        }
        
        return "Below Passing (< 60)";
    }
    
    /**
     * Data Model
     * 
     * Immutable value object representing one course entry in the table.
     *
     * Individual exam fields (w1–p2) use boxed Double so null can represent
     * "exam not yet taken" — distinct from a zero score.  p5 and average
     * are derived at construction time and stored for efficient table
     * rendering and GPA calculation without re-computing on every grid refresh.
     */ 
    private static class GradeRow 
    {
        private final String className;
        private final double classHours;
        private final Double w1;
        private final Double w2;
        private final Double p1;
        private final Double p2;
        private final double p5;
        private final double average;
 
        GradeRow(String className, double classHours, Double w1, Double w2, Double p1, Double p2, double p5, double average) 
        {
            this.className  = className;
            this.classHours = classHours;
            this.w1 = w1; 
            this.w2 = w2; 
            this.p1 = p1; 
            this.p2 = p2;
            this.p5 = p5; 
            this.average = average;
        }
 
        public String getClassName()  
        { 
            return className; 
        }
        public double getClassHours() 
        { 
            return classHours; 
        }
        public Double getW1()         
        { 
            return w1; 
        }
        public Double getW2()         
        { 
            return w2; 
        }
        public Double getP1()         
        { 
            return p1; 
        }
        public Double getP2()         
        { 
            return p2; 
        }
        public double getP5()         
        { 
            return p5; 
        }
        public double getAverage()    
        { 
            return average;
        }
        public boolean isAnyEntered() 
        {
            return w1 != null || w2 != null || p1 != null || p2 != null;
        }
    }
}