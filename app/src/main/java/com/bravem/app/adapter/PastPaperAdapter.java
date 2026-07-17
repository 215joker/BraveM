package com.bravem.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.model.PastPaper;
import com.bravem.app.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of past papers with course, year, semester, and file-type info.
 * Tapping a row opens the paper viewer; the download icon triggers a direct download.
 */
public class PastPaperAdapter extends RecyclerView.Adapter<PastPaperAdapter.PaperViewHolder> {

    public interface OnPaperActionListener {
        void onPaperClick(PastPaper paper);
        void onDownloadClick(PastPaper paper);
        void onPinClick(PastPaper paper);
    }

    public interface OnAdminActionListener {
        void onApproveToggle(PastPaper paper, boolean approve);
        void onDelete(PastPaper paper);
    }

    private final List<PastPaper> papers = new ArrayList<>();
    private final List<PastPaper> allPapers = new ArrayList<>();
    private final OnPaperActionListener actionListener;
    private OnAdminActionListener adminActionListener; // optional

    public PastPaperAdapter(OnPaperActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setAdminActionListener(OnAdminActionListener listener) {
        this.adminActionListener = listener;
    }

    public void submitList(List<PastPaper> newPapers) {
        allPapers.clear();
        allPapers.addAll(newPapers);
        filter("");
    }

    public void filter(String query) {
        papers.clear();
        String lowerQuery = query.toLowerCase();
        for (PastPaper p : allPapers) {
            if (p.getTitle().toLowerCase().contains(lowerQuery) ||
                    (p.getCourseCode() != null && p.getCourseCode().toLowerCase().contains(lowerQuery)) ||
                    (p.getCourseName() != null && p.getCourseName().toLowerCase().contains(lowerQuery))) {
                papers.add(p);
            }
        }
        notifyDataSetChanged();
    }

    public void addAll(List<PastPaper> morePapers) {
        int start = papers.size();
        papers.addAll(morePapers);
        notifyItemRangeInserted(start, morePapers.size());
    }

    public boolean isEmpty() {
        return papers.isEmpty();
    }

    @NonNull
    @Override
    public PaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_past_paper, parent, false);
        return new PaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaperViewHolder holder, int position) {
        holder.bind(papers.get(position));
    }

    @Override
    public int getItemCount() {
        return papers.size();
    }

    class PaperViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView metaView;
        private final TextView fileTypeBadge;
        private final ImageView downloadButton;
        private final ImageView pinButton;
        private final View adminBar;
        private final TextView approveToggle;
        private final View deleteButton;

        PaperViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.text_paper_title);
            metaView = itemView.findViewById(R.id.text_paper_meta);
            fileTypeBadge = itemView.findViewById(R.id.badge_file_type);
            downloadButton = itemView.findViewById(R.id.btn_download);
            pinButton = itemView.findViewById(R.id.btn_pin);
            adminBar = itemView.findViewById(R.id.layout_admin_bar);
            approveToggle = itemView.findViewById(R.id.text_approve_toggle);
            deleteButton = itemView.findViewById(R.id.btn_delete_paper);
        }

        void bind(PastPaper paper) {
            titleView.setText(paper.getTitle());

            String meta = paper.getCourseCode() != null ? paper.getCourseCode() + " · " : "";
            meta += paper.getYear() > 0 ? paper.getYear() + " · " : "";
            meta += paper.getSemester() != null ? paper.getSemester() : "";
            meta += paper.getFileSizeBytes() > 0
                    ? " · " + FileUtils.humanReadableSize(paper.getFileSizeBytes())
                    : "";
            metaView.setText(meta);

            fileTypeBadge.setText(PastPaper.TYPE_PDF.equals(paper.getFileType()) ? "PDF" : "DOCX");

            itemView.setOnClickListener(v -> actionListener.onPaperClick(paper));
            downloadButton.setOnClickListener(v -> actionListener.onDownloadClick(paper));
            
            if (pinButton != null) {
                pinButton.setImageResource(paper.isPinned() ? R.drawable.ic_pin : R.drawable.ic_pin_outline);
                pinButton.setOnClickListener(v -> actionListener.onPinClick(paper));
            }

            if (adminActionListener != null) {
                adminBar.setVisibility(View.VISIBLE);
                approveToggle.setText(paper.isApproved() ? "Unapprove" : "Approve");
                approveToggle.setOnClickListener(v ->
                        adminActionListener.onApproveToggle(paper, !paper.isApproved()));
                deleteButton.setOnClickListener(v -> adminActionListener.onDelete(paper));
            } else {
                adminBar.setVisibility(View.GONE);
            }
        }
    }
}
