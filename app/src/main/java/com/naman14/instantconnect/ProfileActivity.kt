package com.naman14.instantconnect

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.GetAllSocialsAndNftsQuery
import com.google.android.material.carousel.CarouselLayoutManager
import com.naman14.airstackandroid.AirStack
import com.naman14.instantconnect.carousel.CarouselAdapter
import com.naman14.instantconnect.carousel.CarouselItem
import com.naman14.instantconnect.databinding.ActivityProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class ProfileActivity: AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var address: String

    private lateinit var poapAdapter: CarouselAdapter
    private lateinit var nftAdapter: CarouselAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        address = intent.getStringExtra("address") ?: ""

        val poapRecyclerView: RecyclerView = binding.poapRecyclerView
        val multiBrowseCenteredCarouselLayoutManager = CarouselLayoutManager()
        poapRecyclerView.layoutManager = multiBrowseCenteredCarouselLayoutManager
        poapRecyclerView.isNestedScrollingEnabled = false

        poapAdapter =
            CarouselAdapter { item, position ->
                poapRecyclerView.scrollToPosition(
                    position
                )
            }
        poapRecyclerView.adapter = poapAdapter


        val nftsRecyclerView: RecyclerView = binding.nftRecyclerView
        val multiBrowseCenteredCarouselLayoutManager2 = CarouselLayoutManager()
        nftsRecyclerView.layoutManager = multiBrowseCenteredCarouselLayoutManager2
        nftsRecyclerView.isNestedScrollingEnabled = false

        nftAdapter =
            CarouselAdapter { item, position ->
                nftsRecyclerView.scrollToPosition(
                    position
                )
            }
        nftsRecyclerView.adapter = nftAdapter

        fetchData()

    }

    private fun fetchData() {
        GlobalScope.launch(Dispatchers.IO) {
            val data = AirStack.getSocialProfile(address)
            if (data != null) {
                GlobalScope.launch(Dispatchers.Main) {
                    showData(data!!)
                }
            }
        }
    }

    private fun showData(data: GetAllSocialsAndNftsQuery.Data) {
        if (data.Socials!!.Social!!.isNotEmpty()) {
            Log.d("lol", data.Socials!!.Social!!.toString())
            val lensSocial = data.Socials!!.Social!!.find { it.dappName!!.name == "lens" }
            if (lensSocial != null) {
                binding.lensProfileTitle.text = lensSocial.profileImage
                binding.lensProfileSubtitle.text = lensSocial.profileBio
                binding.lensProfileImage.loadUrl(lensSocial.profileImage.orEmpty())
            }
        }

        if (data.Poaps != null && data.Poaps!!.Poap != null) {
            val poaps = data.Poaps!!.Poap!!
            poapAdapter.submitList(poaps.map {
                    CarouselItem(it.poapEvent!!.contentValue!!.image!!.medium, it.poapEvent!!.eventName)
            })
        }

        if (data.TokenBalances != null && data.TokenBalances!!.TokenBalance != null) {
            val nfts = data.TokenBalances!!.TokenBalance
            nftAdapter.submitList(nfts!!.map {
                CarouselItem(it.tokenNfts!!.contentValue!!.image!!.small, it.tokenNfts!!.metaData!!.name)
            })
        }
    }
}