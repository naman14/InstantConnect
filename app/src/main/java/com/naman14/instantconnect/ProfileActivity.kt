package com.naman14.instantconnect

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.GetAllSocialsAndNftsQuery
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.HeroCarouselStrategy
import com.naman14.airstackandroid.AirStack
import com.naman14.instantconnect.carousel.CarouselAdapter
import com.naman14.instantconnect.carousel.CarouselItem
import com.naman14.instantconnect.carousel.ChipAdapter
import com.naman14.instantconnect.databinding.ActivityProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.AccessController.getContext
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity: AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var address: String

    private lateinit var poapAdapter: CarouselAdapter
    private lateinit var nftAdapter: CarouselAdapter
    private lateinit var poapCitiesAdapter: ChipAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        address = intent.getStringExtra("address") ?: ""

        val poapRecyclerView: RecyclerView = binding.poapRecyclerView
        val multiBrowseCenteredCarouselLayoutManager = CarouselLayoutManager(HeroCarouselStrategy())
        poapRecyclerView.layoutManager = multiBrowseCenteredCarouselLayoutManager
        poapRecyclerView.isNestedScrollingEnabled = false

        poapAdapter =
            CarouselAdapter { item, position ->
                poapRecyclerView.scrollToPosition(
                    position
                )
            }

        val snapHelper = CarouselSnapHelper()
        snapHelper.attachToRecyclerView(poapRecyclerView)

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


        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.poapCitiesRecyclerview.layoutManager = layoutManager

        poapCitiesAdapter = ChipAdapter()
        binding.poapCitiesRecyclerview.adapter = poapCitiesAdapter

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
            val lensSocial = data.Socials!!.Social!!.find { it.dappName!!.toString() == "lens" }
            val farcasterSocial = data.Socials!!.Social!!.find { it.dappName!!.toString() == "farcaster" }
            if (lensSocial != null) {
                binding.lensProfileTitle.text = lensSocial.profileName
                binding.lensProfileSubtitle.text = lensSocial.profileBio
                if (lensSocial.profileImage.isNullOrEmpty()) {
                    if (lensSocial.tokenNft!!.contentValue != null && lensSocial.tokenNft!!.contentValue!!.image != null)
                        binding.lensProfileImage.loadUrl(lensSocial.tokenNft!!.contentValue!!.image!!.small!!)
                } else {
                    binding.lensProfileImage.loadUrl(lensSocial.profileImage!!)
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                val date = dateFormat.parse(lensSocial.userCreatedAtBlockTimestamp as String)
                val cal = Calendar.getInstance()
                cal.timeInMillis = date!!.time

                binding.lensProfileSubtitle.text = "Joined ${cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.getDefault())} ${cal.get(Calendar.YEAR)}"

                binding.lensProfileSubtitle2.text = "${lensSocial!!.followerCount} followers • ${lensSocial!!.followingCount} following"
            }

            if (farcasterSocial != null) {
                binding.fcProfileTitle.text = farcasterSocial.profileName
                binding.fcProfileSubtitle.text = farcasterSocial.profileBio

                if (farcasterSocial.profileImage.isNullOrEmpty()) {
                    if (farcasterSocial.tokenNft!!.contentValue != null && farcasterSocial.tokenNft!!.contentValue!!.image != null)
                        binding.fcProfileImage.loadUrl(farcasterSocial.tokenNft!!.contentValue!!.image!!.small!!)
                } else {
                    binding.fcProfileImage.loadUrl(farcasterSocial.profileImage!!)
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                val date = dateFormat.parse(farcasterSocial.userCreatedAtBlockTimestamp as String)
                val cal = Calendar.getInstance()
                cal.timeInMillis = date!!.time

                binding.fcProfileSubtitle.text = "Joined ${cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.getDefault())} ${cal.get(Calendar.YEAR)}"

                binding.fcProfileSubtitle2.text = "${farcasterSocial!!.followerCount} followers • ${farcasterSocial!!.followingCount} following"
            }
        }

        if (data.XMTPs != null && data.XMTPs!!.XMTP != null && data.XMTPs!!.XMTP!!.isNotEmpty()) {
            binding.ivXmtp.setImageResource(R.drawable.baseline_done_24)
            binding.ivXmtp.setBackgroundColor(android.graphics.Color.parseColor("#AAF8B8"))
            binding.tvXmtp.text = "XMTP enabled"
        }

        if (data.Poaps != null && data.Poaps!!.Poap != null) {
            val poaps = data.Poaps!!.Poap!!
            poapAdapter.submitList(poaps.filter { it.poapEvent != null && it.poapEvent!!.contentValue != null && it.poapEvent!!.contentValue!!.image != null}.map {
                    CarouselItem(it.poapEvent!!.contentValue!!.image!!.medium, it.poapEvent!!.eventName)
            })

            binding.tvPoaps.text = "POAPs(${poaps.size})"

            val events = poaps.distinctBy { it.poapEvent!!.eventName }
            val cities = poaps.distinctBy { it.poapEvent!!.city }.filter { it.poapEvent!!.city!!.isNotEmpty() }

            val numCities = cities.size
            val numEvents = events.size

            binding.tvNumCities.text = numCities.toString() + " cities"
            binding.tvNumEvents.text = numEvents.toString() + " events"

            poapCitiesAdapter.setData(cities.map { it.poapEvent!!.city!! + ", " + it.poapEvent!!.country })
        }


        if (data.TokenBalances != null && data.TokenBalances!!.TokenBalance != null) {
            val nfts = data.TokenBalances!!.TokenBalance
            nftAdapter.submitList(nfts!!.filter { it.tokenNfts != null && it.tokenNfts!!.contentValue != null && it.tokenNfts!!.contentValue!!.image != null}.map {
                CarouselItem(it.tokenNfts!!.contentValue!!.image!!.small, it.tokenNfts!!.metaData!!.name)
            })

            binding.tvNfts.text = "NFTs(${nfts.size})"
        }
    }
}