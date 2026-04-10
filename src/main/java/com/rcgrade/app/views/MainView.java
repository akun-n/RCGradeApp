package com.rcgrade.app.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("RCGrade App")
public class MainView extends VerticalLayout {

    public MainView() {
        TextField nameField = new TextField("Your name");
        Button button = new Button("Say hello");
        Text greeting = new Text("");

        button.addClickListener(event -> {
            String name = nameField.getValue().trim();
            if (name.isEmpty()) {
                greeting.setText("Hello!");
            } else {
                greeting.setText("Hello, " + name + "!");
            }
        });

        add(nameField, button, greeting);
    }
}
