package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SupportActivity extends AppCompatActivity {

    // One FAQ entry and the view that shows it
    private static class Faq {
        final String question;
        final String answer;
        View row;
        TextView tvAnswer;
        TextView tvArrow;
        boolean expanded = false;

        Faq(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }

    private final List<Faq> faqs = new ArrayList<>();
    private LinearLayout faqContainer;
    private TextView tvNoResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_support);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupBack(this);

        faqContainer = findViewById(R.id.faqContainer);
        tvNoResults = findViewById(R.id.tvNoResults);
        EditText etSearch = findViewById(R.id.etSearch);

        findViewById(R.id.btnOpenCase).setOnClickListener(v ->
                startActivity(new Intent(this, OpenCaseActivity.class)));

        buildFaqList();

        // Filter the questions as the user types
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFaqs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void buildFaqList() {
        faqs.add(new Faq("How do I book a service?",
                "Search for a business or freelancer, open their profile, choose a service and send a booking request."));
        faqs.add(new Faq("How do I reset my password?",
                "On the sign-in screen, tap Forgot password and enter your email to get a reset link. "
                        + "If you are already signed in, open Security and choose Change password."));
        faqs.add(new Faq("How are freelancers verified?",
                "New freelancers start as Pending Approval. An administrator reviews each registration and "
                        + "approves it. Approved freelancers show a Verified badge on their profile."));
        faqs.add(new Faq("How do I cancel a booking?",
                "Open My Bookings to see the status of your booking. To cancel, contact the provider, "
                        + "or open a support case below and our team will help."));
        faqs.add(new Faq("How do I leave a review?",
                "Once a booking is completed, you can rate the provider and add a short comment about your experience."));

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Faq faq : faqs) {
            View row = inflater.inflate(R.layout.item_faq, faqContainer, false);
            TextView tvQuestion = row.findViewById(R.id.tvQuestion);
            faq.tvAnswer = row.findViewById(R.id.tvAnswer);
            faq.tvArrow = row.findViewById(R.id.tvArrow);
            faq.row = row;

            tvQuestion.setText(faq.question);
            faq.tvAnswer.setText(faq.answer);

            row.setOnClickListener(v -> toggle(faq));
            faqContainer.addView(row);
        }
    }

    // Open the tapped answer and close the others
    private void toggle(Faq tapped) {
        boolean open = !tapped.expanded;
        for (Faq faq : faqs) {
            setExpanded(faq, faq == tapped && open);
        }
    }

    private void setExpanded(Faq faq, boolean expanded) {
        faq.expanded = expanded;
        faq.tvAnswer.setVisibility(expanded ? View.VISIBLE : View.GONE);
        faq.tvArrow.setRotation(expanded ? 90f : 0f);
    }

    private void filterFaqs(String rawQuery) {
        String query = rawQuery.trim().toLowerCase(Locale.ROOT);
        int shown = 0;

        for (Faq faq : faqs) {
            boolean match = query.isEmpty()
                    || faq.question.toLowerCase(Locale.ROOT).contains(query)
                    || faq.answer.toLowerCase(Locale.ROOT).contains(query);
            faq.row.setVisibility(match ? View.VISIBLE : View.GONE);
            if (match) shown++;
        }

        tvNoResults.setVisibility(shown == 0 ? View.VISIBLE : View.GONE);
    }
}