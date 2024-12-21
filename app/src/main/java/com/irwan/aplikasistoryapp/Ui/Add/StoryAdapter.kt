import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.irwan.aplikasistoryapp.R
import com.irwan.aplikasistoryapp.api.Story

class StoryAdapter(
    private val listStory: List<Story>
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    // ViewHolder class for binding views
    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoto: ImageView = itemView.findViewById(R.id.iv_photo)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
    }

    // Inflate item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    // Bind data to the views
    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = listStory[position]
        holder.tvTitle.text = story.name
        holder.tvDescription.text = story.description

        // Load image from URI or URL
        if (story.photoUrl.startsWith("content://")) {
            // If photoUrl is a local URI, load it directly
            holder.ivPhoto.setImageURI(Uri.parse(story.photoUrl))
        } else {
            // Otherwise, load it using Glide (network URL)
            Glide.with(holder.itemView.context)
                .load(story.photoUrl)
                .placeholder(R.drawable.placeholder_image) // Optional: Placeholder image
                .error(R.drawable.error_image) // Optional: Error image
                .into(holder.ivPhoto)
        }
    }

    // Return the size of the dataset
    override fun getItemCount(): Int = listStory.size
}
