package com.example.campusmarket

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarket.data.model.MyStoreLatestProduct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

class MyMarketPostAdapter(
    private val items: List<MyStoreLatestProduct>
) : RecyclerView.Adapter<MyMarketPostAdapter.MyMarketPostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyMarketPostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_market_post, parent, false)
        return MyMarketPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyMarketPostViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class MyMarketPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProduct: ImageView = itemView.findViewById(R.id.ivProduct)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val ivHeart: ImageView = itemView.findViewById(R.id.ivHeart)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)

        private var imageJob: Job? = null

        fun bind(item: MyStoreLatestProduct) {
            tvTitle.text = item.productName
            tvPrice.text = formatPrice(item.price)

            // /my-store 응답에는 날짜/찜 수가 없어서 숨김
            tvDate.visibility = View.GONE
            ivHeart.visibility = View.GONE
            tvLikeCount.visibility = View.GONE

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ProductDetailActivity::class.java)
                intent.putExtra("productId", item.productId)
                context.startActivity(intent)
            }

            imageJob?.cancel()
            ivProduct.setImageDrawable(null)

            val imageUrl = item.thumbnailImageUrl

            if (!imageUrl.isNullOrBlank()) {
                imageJob = CoroutineScope(Dispatchers.Main).launch {
                    val bitmap = withContext(Dispatchers.IO) {
                        loadBitmapFromUrl(imageUrl)
                    }

                    if (bitmap != null) {
                        ivProduct.setImageBitmap(bitmap)
                    } else {
                        ivProduct.setImageResource(R.drawable.clothes12)
                    }
                }
            } else {
                ivProduct.setImageResource(R.drawable.clothes12)
            }
        }

        private fun formatPrice(price: Int): String {
            return NumberFormat.getNumberInstance(Locale.KOREA).format(price) + "원"
        }

        private fun loadBitmapFromUrl(imageUrl: String): Bitmap? {
            return try {
                val url = URL(imageUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    doInput = true
                    connect()
                }

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    connection.disconnect()
                    return null
                }

                val stream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(stream)
                stream.close()
                connection.disconnect()
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }
}