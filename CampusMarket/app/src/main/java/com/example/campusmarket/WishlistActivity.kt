package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusmarket.data.model.WishlistProduct
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class WishlistActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        recyclerView = findViewById(R.id.recyclerWishlist)
        tvEmpty = findViewById(R.id.tvEmpty)

        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        loadWishlist()
    }

    private fun loadWishlist() {
        val guestUuid = GuestManager.getGuestUuid(this)
        if (guestUuid.isNullOrBlank()) {
            showEmpty()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getWishlistProducts(guestUuid)
                if (response.isSuccessful) {
                    val products = response.body()?.result?.products ?: emptyList()
                    if (products.isEmpty()) {
                        showEmpty()
                    } else {
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        recyclerView.adapter = WishlistAdapter(products) { productId ->
                            val intent = Intent(this@WishlistActivity, ProductDetailActivity::class.java)
                            intent.putExtra("productId", productId)
                            startActivity(intent)
                        }
                    }
                } else {
                    Toast.makeText(this@WishlistActivity, "불러오기 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    showEmpty()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@WishlistActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                showEmpty()
            }
        }
    }

    private fun showEmpty() {
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.VISIBLE
    }

    // ─── Adapter ─────────────────────────────────────────────────────────────

    class WishlistAdapter(
        private val items: List<WishlistProduct>,
        private val onClick: (Long) -> Unit
    ) : RecyclerView.Adapter<WishlistAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivThumb: ImageView = view.findViewById(R.id.ivWishlistThumb)
            val tvName: TextView = view.findViewById(R.id.tvWishlistName)
            val tvPrice: TextView = view.findViewById(R.id.tvWishlistPrice)
            val tvStatus: TextView = view.findViewById(R.id.tvWishlistStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_wishlist_product, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvName.text = item.productName ?: "상품명 없음"
            holder.tvPrice.text = item.price?.let {
                NumberFormat.getNumberInstance(Locale.KOREA).format(it) + "원"
            } ?: "가격 미정"
            holder.tvStatus.text = when (item.saleStatus) {
                "ON_SALE" -> "판매중"
                "SOLD" -> "판매완료"
                "RESERVED" -> "예약중"
                else -> item.saleStatus ?: ""
            }

            if (!item.thumbnailImageUrl.isNullOrBlank()) {
                Glide.with(holder.itemView.context)
                    .load(item.thumbnailImageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivThumb)
            }

            holder.itemView.setOnClickListener { onClick(item.productId) }
        }

        override fun getItemCount(): Int = items.size
    }
}
