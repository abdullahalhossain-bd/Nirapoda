package com.example.nirapod.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nirapod.R;
import com.example.nirapod.models.Review;

import java.util.List;



public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> reviewList;
    private Context context;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_healthcare, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.authorName.setText(review.getAuthorName());
        holder.reviewDate.setText(review.getFormattedDate());
        holder.reviewContent.setText(review.getContent());
        holder.ratingBar.setRating(review.getRating());
        holder.likeCount.setText(String.valueOf(review.getLikes()));
        holder.dislikeCount.setText(String.valueOf(review.getDislikes()));

        holder.likeButton.setOnClickListener(v -> {
            review.incrementLikes();
            holder.likeCount.setText(String.valueOf(review.getLikes()));
            Toast.makeText(context, "You liked a review", Toast.LENGTH_SHORT).show();
        });

        holder.dislikeButton.setOnClickListener(v -> {
            review.incrementDislikes();
            holder.dislikeCount.setText(String.valueOf(review.getDislikes()));
            Toast.makeText(context, "You disliked a review", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void addReview(Review review) {
        reviewList.add(0, review); // Add at the beginning to show newest first
        notifyItemInserted(0);
    }

    public void updateReviews(List<Review> reviews) {
        this.reviewList = reviews;
        notifyDataSetChanged();
    }

    public interface OnReviewInteractionListener {
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView authorName;
        TextView reviewDate;
        TextView reviewContent;
        RatingBar ratingBar;
        ImageButton likeButton;
        TextView likeCount;
        ImageButton dislikeButton;
        TextView dislikeCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            authorName = itemView.findViewById(R.id.tvReviewAuthor);
            reviewDate = itemView.findViewById(R.id.tvReviewDate);
            reviewContent = itemView.findViewById(R.id.tvReviewContent);
            ratingBar = itemView.findViewById(R.id.rbReviewRating);
            likeButton = itemView.findViewById(R.id.btnLike);
            likeCount = itemView.findViewById(R.id.tvLikeCount);
            dislikeButton = itemView.findViewById(R.id.btnDislike);
            dislikeCount = itemView.findViewById(R.id.tvDislikeCount);
        }
    }
}
