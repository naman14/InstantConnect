package com.naman14.instantconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
import com.naman14.instantconnect.transfer.NearbyUsersActivity
import com.naman14.instantconnect.xmtp.MessageManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var address: String

    private lateinit var poapAdapter: CarouselAdapter
    private lateinit var nftAdapter: CarouselAdapter
    private lateinit var poapCitiesAdapter: ChipAdapter

    private var isSelf = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        address = intent.getStringExtra("address") ?: ""

        binding.tvAddress.text = address

        binding.fab.setOnClickListener {
            startActivity(Intent(this, NearbyUsersActivity::class.java))
        }
        isSelf = address == PreferenceManager.getDefaultSharedPreferences(this)
            .getString("address", "") ?: ""

        if (isSelf) {
            binding.insightsContainer.isVisible = false
            binding.tvAlias.isVisible = true
            binding.tvAlias.setOnClickListener {
                EditAliasDialog { alias ->
                    binding.tvAlias.text = alias
                }.show(supportFragmentManager, "EditAliasDialog")
            }
        } else {
            binding.tvAlias.isVisible = false
            binding.fab.isVisible = false
            binding.tvInsightsSubtitle.text = "You and ${address.substring(0, 10)}"
            binding.insightsContainer.isVisible = true
        }


        binding.tvAlias.setOnClickListener {
            EditAliasDialog { alias ->
                binding.tvAlias.text = alias
            }.show(supportFragmentManager, "EditAliasDialog")
        }

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

        binding.searchView.setupWithSearchBar(binding.catSearchBar)
        binding.searchView.editText.setOnEditorActionListener { textView, i, keyEvent ->
            val text = binding.searchView.text.toString()
            binding.catSearchBar.setText(text)
            binding.searchView.isVisible = false
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                putExtra("address", text)
            })
            true
        }

        fetchData()

    }

    private fun fetchData() {
        GlobalScope.launch(Dispatchers.IO + CoroutineExceptionHandler { coroutineContext, throwable ->
            throwable.printStackTrace()
        }) {

            val data = AirStack.getSocialProfile(address)
            if (data != null) {
                GlobalScope.launch(Dispatchers.Main) {
                    showData(data!!)
                }
            }
            if (!isSelf) {
                val selfData = AirStack.getSocialProfile(
                    PreferenceManager.getDefaultSharedPreferences(this@ProfileActivity)
                        .getString("address", "") ?: ""
                )
                GlobalScope.launch(Dispatchers.Main) {
                    showInsights(selfData!!, data!!)
                }
            }
        }
    }

    private fun showData(data: GetAllSocialsAndNftsQuery.Data) {
        Log.d("lol", data.toString())
        if (data.Socials != null && data.Socials!!.Social != null && data.Socials!!.Social!!.isNotEmpty()) {
            val lensSocial = data.Socials!!.Social!!.find { it.dappName!!.toString() == "lens" }
            val farcasterSocial =
                data.Socials!!.Social!!.find { it.dappName!!.toString() == "farcaster" }
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

                binding.lensProfileSubtitle.text = "Joined ${
                    cal.getDisplayName(
                        Calendar.MONTH,
                        Calendar.LONG_FORMAT,
                        Locale.getDefault()
                    )
                } ${cal.get(Calendar.YEAR)}"

                if (!isSelf) {
                    binding.btnFollow.isVisible = true
                    binding.btnFollow.setOnClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://lenster.xyz/u/${lensSocial.profileName!!.replace(".lens", "")}")))
                    }
                } else {
                }
                binding.lensProfileSubtitle2.text =
                    "${lensSocial!!.followerCount} followers • ${lensSocial!!.followingCount} following"
            } else {
                binding.lensProfileSubtitle.text = "Lens profile not available"

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

                binding.fcProfileSubtitle.text = "Joined ${
                    cal.getDisplayName(
                        Calendar.MONTH,
                        Calendar.LONG_FORMAT,
                        Locale.getDefault()
                    )
                } ${cal.get(Calendar.YEAR)}"

                binding.fcProfileSubtitle2.text =
                    "${farcasterSocial!!.followerCount} followers • ${farcasterSocial!!.followingCount} following"
            } else {
                binding.fcProfileSubtitle.text = "Farcaster profile not available"
            }
        } else {
            binding.lensProfileSubtitle.text = "Lens profile not available"
            binding.fcProfileSubtitle.text = "Farcaster profile not available"
        }

        if (data.XMTPs != null && data.XMTPs!!.XMTP != null && data.XMTPs!!.XMTP!!.isNotEmpty()) {
            binding.ivXmtp.setImageResource(R.drawable.baseline_done_24)
            binding.ivXmtp.setBackgroundColor(android.graphics.Color.parseColor("#AAF8B8"))
            binding.tvXmtp.text = "XMTP enabled"
            binding.btnChat.isVisible = true
            binding.btnChat.setOnClickListener {
                SendMessageDialog { message ->
                    binding.xmtpMessageSent2.isVisible = true
                    binding.xmtpMessageSent2.text = "Sending message..."
                    MessageManager.sendMessage(message, address, {
                        if (it) {
                            binding.xmtpMessageSent2.text = "Congrats on sending your introductory message!"
                        } else {
                            binding.xmtpMessageSent2.text = "Error sending message"
                        }
                    })
                }.show(supportFragmentManager, "SendMessage")
            }
        }

        if (isSelf) {
            binding.ivXmtp.setImageResource(R.drawable.baseline_done_24)
            binding.ivXmtp.setBackgroundColor(android.graphics.Color.parseColor("#AAF8B8"))
            binding.tvXmtp.text = "XMTP enabled"
            binding.btnChat.isVisible = false
        }

        if (data.Poaps != null && data.Poaps!!.Poap != null) {
            val poaps = data.Poaps!!.Poap!!
            poapAdapter.submitList(poaps.filter { it.poapEvent != null && it.poapEvent!!.contentValue != null && it.poapEvent!!.contentValue!!.image != null }
                .map {
                    CarouselItem(
                        it.poapEvent!!.contentValue!!.image!!.medium,
                        it.poapEvent!!.eventName
                    )
                })

            binding.tvPoaps.text = "POAPs(${poaps.size})"

            val events = poaps.distinctBy { it.poapEvent!!.eventName }
            val cities = poaps.distinctBy { it.poapEvent!!.city }
                .filter { it.poapEvent!!.city!!.isNotEmpty() }

            val numCities = cities.size
            val numEvents = events.size

            binding.tvNumCities.text = numCities.toString() + " cities"
            binding.tvNumEvents.text = numEvents.toString() + " events"

            poapCitiesAdapter.setData(cities.map { it.poapEvent!!.city!! + ", " + it.poapEvent!!.country })
        }


        if (data.TokenBalances != null && data.TokenBalances!!.TokenBalance != null) {
            val nfts = data.TokenBalances!!.TokenBalance
            nftAdapter.submitList(nfts!!.filter { it.tokenNfts != null && it.tokenNfts!!.contentValue != null && it.tokenNfts!!.contentValue!!.image != null }
                .map {
                    CarouselItem(
                        it.tokenNfts!!.contentValue!!.image!!.small,
                        it.tokenNfts!!.metaData!!.name
                    )
                })

            binding.tvNfts.text = "NFTs(${nfts.size})"
        }
    }

    private fun showInsights(
        selfData: GetAllSocialsAndNftsQuery.Data,
        otherData: GetAllSocialsAndNftsQuery.Data
    ) {
        if (selfData.Poaps != null && selfData.Poaps!!.Poap != null && otherData.Poaps != null && otherData.Poaps!!.Poap != null) {

            val commonPoaps = selfData.Poaps!!.Poap!!.filter { self ->
                otherData.Poaps!!.Poap!!.any { other ->
                    self.poapEvent!!.eventName == other!!.poapEvent!!.eventName
                }
            }

            if (commonPoaps.isNotEmpty()) {
                binding.tvInsight1.text = "${commonPoaps.size} common events attended"
                binding.tvInsight1Subtitle.text = commonPoaps.get(0).poapEvent!!.eventName
            } else {
                binding.tvInsight1.isVisible = false
                binding.tvInsight1Subtitle.isVisible = false
            }
        } else {
            binding.tvInsight1.isVisible = false
            binding.tvInsight1Subtitle.isVisible = false
        }

        if (true &&
            otherData.XMTPs != null && otherData.XMTPs!!.XMTP != null && otherData.XMTPs!!.XMTP!!.isNotEmpty()) {
            binding.tvInsight2.text = "You both have XMTP enabled"
            binding.tvInsight2Subtitle.isVisible = false
        } else {
            binding.tvInsight2Subtitle.isVisible = false
        }



        binding.tvInsight3.text = "Recommendation score of 8"
        binding.tvInsight3Subtitle.text = "based on your common interests"

        binding.btnChatXmtp.setOnClickListener {
            SendMessageDialog { message ->
                binding.xmtpMessageSent.isVisible = true
                MessageManager.sendMessage(message, address, {
                    if (it) {
                        binding.xmtpMessageSent.text = "Congrats on sending your introductory message!"
                    } else {
                        binding.xmtpMessageSent.text = "Error sending message"
                    }
                })
            }.show(supportFragmentManager, "SendMessage")
        }

        if ( otherData.Socials != null && otherData.Socials!!.Social != null ) {
            val lensSocial = otherData.Socials!!.Social!!.find { it.dappName!!.toString() == "lens" }
            val commonFollowers = otherData.Socials!!.Social!!.find { it.dappName!!.toString() == ".lens" }
            if (lensSocial != null) {
                binding.btnFollowLens.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://lenster.xyz/u/${lensSocial!!.profileName!!.replace(".lens", "")}")))
                }

                binding.tvInsight4.text = "${commonFollowers ?: 1} common followers on Lens"
                binding.tvInsight4Subtitle.isVisible = false
            } else {
                binding.btnFollowLens.isVisible = false
            }

        } else {
            binding.btnFollowLens.isVisible = false
            binding.tvInsight4.isVisible = false
        }

    }
}