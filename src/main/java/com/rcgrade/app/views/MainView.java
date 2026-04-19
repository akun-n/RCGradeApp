package com.rcgrade.app.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;

@Route("")
@PageTitle("RCGrade App")
public class MainView extends VerticalLayout {

    public MainView() {
        TextField classField = new TextField("Other Class Name");
        classField.setPlaceholder("Enter custom class name");
        classField.setVisible(false);
        Button button = new Button("Add Class");
        Text greeting = new Text("");
        NumberField w1Field = createGradeField("W1");
        NumberField w2Field = createGradeField("W2");
        NumberField p1Field = createGradeField("P1");
        NumberField p2Field = createGradeField("P2");
        NumberField p5Field = createGradeField("P5 (Best)");
        NumberField avgField = createGradeField("Average");
        NumberField classHoursField = new NumberField("Class Hours / Week");
        p5Field.setReadOnly(true);
        avgField.setReadOnly(true);
        classHoursField.setStep(1);
        classHoursField.setMin(0);
        classHoursField.setValue(0.0);

        Grid<GradeRow> classTable = new Grid<>(GradeRow.class, false);
        List<GradeRow> classRows = new ArrayList<>();
        Grid.Column<GradeRow> classNameColumn = classTable.addColumn(GradeRow::getClassName).setHeader("Class");
        Grid.Column<GradeRow> classHoursColumn = classTable.addColumn(GradeRow::getClassHours).setHeader("Hours/Week");
        classTable.addColumn(GradeRow::getW1).setHeader("W1");
        classTable.addColumn(GradeRow::getW2).setHeader("W2");
        classTable.addColumn(GradeRow::getP1).setHeader("P1");
        classTable.addColumn(GradeRow::getP2).setHeader("P2");
        classTable.addColumn(GradeRow::getP5).setHeader("P5 (Best)");
        Grid.Column<GradeRow> averageColumn = classTable.addColumn(GradeRow::getAverage).setHeader("Average");
        FooterRow footerRow = classTable.appendFooterRow();
        footerRow.getCell(classNameColumn).setText("Weighted GPA");
        footerRow.getCell(classHoursColumn).setText("-");
        footerRow.getCell(averageColumn).setText("0.00");
        classTable.setVisible(false);

        H3 catalogTitle = new H3("Course Catalog");
        final String[] selectedCourse = {""};

        List<CourseCatalogItem> catalogItems = List.of(
                new CourseCatalogItem("Math", 8),
                new CourseCatalogItem("Science", 6),
                new CourseCatalogItem("History", 5),
                new CourseCatalogItem("English", 7),
                new CourseCatalogItem("Computer", 6),
                new CourseCatalogItem("Others", 0)
        );

        HorizontalLayout catalogButtonsLayout = new HorizontalLayout();
        catalogButtonsLayout.getStyle().set("flex-wrap", "wrap");
        List<Button> courseButtons = new ArrayList<>();
        for (CourseCatalogItem item : catalogItems) {
            Button courseButton = new Button(item.getCourseName());
            courseButtons.add(courseButton);
            courseButton.addClickListener(event -> {
                for (Button btn : courseButtons) {
                    btn.getStyle().remove("background-color");
                    btn.getStyle().remove("color");
                }
                selectedCourse[0] = item.getCourseName();
                courseButton.getStyle().set("background-color", "#0d6efd");
                courseButton.getStyle().set("color", "#ffffff");
                classField.setVisible("Others".equals(selectedCourse[0]));
                if (!classField.isVisible()) {
                    classField.clear();
                }
            });
            catalogButtonsLayout.add(courseButton);
        }

        button.addClickListener(event -> {
            if (selectedCourse[0].isEmpty()) {
                greeting.setText("Please select a course first.");
                return;
            }
            String className = selectedCourse[0];
            if ("Others".equals(selectedCourse[0])) {
                className = classField.getValue().trim();
                if (className.isEmpty()) {
                    greeting.setText("Please enter a custom class name for Others.");
                    return;
                }
            }

            double w1 = valueOrZero(w1Field.getValue()); //gets the value of the field or 0 if the field is empty
            double w2 = valueOrZero(w2Field.getValue());
            double p1 = valueOrZero(p1Field.getValue());
            double p2 = valueOrZero(p2Field.getValue());
            double p5 = Math.max(Math.max(w1, w2), Math.max(p1, p2));
            double average = (w1 + w2 + p1 + p2) / 4;
            double classHours = valueOrZero(classHoursField.getValue());
            
            p5Field.setValue(p5);
            avgField.setValue(average);
            classRows.add(new GradeRow(className, classHours, w1, w2, p1, p2, p5, average));
            classTable.setItems(classRows);
            footerRow.getCell(averageColumn).setText(String.format("%.2f", calculateWeightedGpa(classRows)));
            classTable.setVisible(true);
            greeting.setText("Class added: " + className + ".");
        });

        add(catalogTitle, catalogButtonsLayout, classField, classHoursField, new HorizontalLayout(w1Field, w2Field, p1Field, p2Field, p5Field, avgField), button, greeting, classTable);
    }

    private NumberField createGradeField(String label) {
        NumberField field = new NumberField(label);
        field.setStep(0.1);
        field.setMin(0);
        field.setMax(100);
        field.setValue(0.0);
        return field;
    }

    private double valueOrZero(Double value) {
        return value == null ? 0.0 : value; //returns 0 if the field is empty or the value of the field if it is not empty
    }

    private double calculateWeightedGpa(List<GradeRow> rows) {
        double totalWeightedGrade = 0.0;
        double totalHours = 0.0;
        for (GradeRow row : rows) {
            totalWeightedGrade += row.getAverage() * row.getClassHours();
            totalHours += row.getClassHours();
        }
        if (totalHours == 0.0) {
            return 0.0;
        }
        return totalWeightedGrade / totalHours;
    }

    private static class GradeRow {
        private String className;
        private double classHours;
        private double w1;
        private double w2;
        private double p1;
        private double p2;
        private double p5;
        private double average;

        private GradeRow(String className, double classHours, double w1, double w2, double p1, double p2, double p5, double average) {
            this.className = className;
            this.classHours = classHours;
            this.w1 = w1;
            this.w2 = w2;
            this.p1 = p1;
            this.p2 = p2;
            this.p5 = p5;
            this.average = average;
        }

        public String getClassName() {
            return className;
        }

        public double getClassHours() {
            return classHours;
        }

        public double getW1() {
            return w1;
        }

        public double getW2() {
            return w2;
        }

        public double getP1() {
            return p1;
        }

        public double getP2() {
            return p2;
        }

        public double getP5() {
            return p5;
        }
        public double getAverage() {
            return average;
        }
    }

    private static class CourseCatalogItem {
        private final String courseName;
        private final int lessonCount;

        private CourseCatalogItem(String courseName, int lessonCount) {
            this.courseName = courseName;
            this.lessonCount = lessonCount;
        }

        public String getCourseName() {
            return courseName;
        }

        public int getLessonCount() {
            return lessonCount;
        }
    }
}
