package com.bravem.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.model.Course;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of Courses within a Degree, e.g. in CourseListActivity
 * or in the admin "Manage Courses" screen.
 */
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public interface OnCourseActionListener {
        void onEdit(Course course);
        void onDelete(Course course);
    }

    private final List<Course> courses = new ArrayList<>();
    private final OnCourseClickListener clickListener;
    private OnCourseActionListener actionListener;

    public CourseAdapter(OnCourseClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setActionListener(OnCourseActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void submitList(List<Course> newCourses) {
        courses.clear();
        courses.addAll(newCourses);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        holder.bind(courses.get(position));
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView codeView;
        private final View adminActions;
        private final View editButton;
        private final View deleteButton;

        CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_course_name);
            codeView = itemView.findViewById(R.id.text_course_code);
            adminActions = itemView.findViewById(R.id.layout_admin_actions);
            editButton = itemView.findViewById(R.id.btn_edit);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }

        void bind(Course course) {
            nameView.setText(course.getName());
            codeView.setText(course.getCode());

            itemView.setOnClickListener(v -> clickListener.onCourseClick(course));

            if (actionListener != null) {
                adminActions.setVisibility(View.VISIBLE);
                editButton.setOnClickListener(v -> actionListener.onEdit(course));
                deleteButton.setOnClickListener(v -> actionListener.onDelete(course));
            } else {
                adminActions.setVisibility(View.GONE);
            }
        }
    }
}
