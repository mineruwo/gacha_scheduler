import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';
import 'dart:async'; // Import for Timer

// Define BannerObject class
class BannerObject {
  final String imageUrl;
  final String description;
  final DateTime startDate;
  final DateTime endDate;
  final Color textColor;

  BannerObject({
    required this.imageUrl,
    required this.description,
    required this.startDate,
    required this.endDate,
    this.textColor = Colors.white,
  });

  // Factory constructor for creating a new BannerObject from a JSON map
  factory BannerObject.fromJson(Map<String, dynamic> json) {
    return BannerObject(
      imageUrl: json['imageUrl'] as String,
      description: json['description'] as String,
      startDate: DateTime.parse(json['startDate'] as String),
      endDate: DateTime.parse(json['endDate'] as String),
      textColor: Color(
        int.parse(json['textColor'] as String),
      ), // Assuming textColor is stored as a hex string like "0xFFFFFFFF"
    );
  }

  // Method for converting a BannerObject to a JSON map
  Map<String, dynamic> toJson() {
    return {
      'imageUrl': imageUrl,
      'description': description,
      'startDate': startDate.toIso8601String(),
      'endDate': endDate.toIso8601String(),
      'textColor':
          '0x${textColor.value.toRadixString(16).padLeft(8, '0')}', // Convert Color to hex string
    };
  }
}

class HomeTabScreen extends StatefulWidget {
  const HomeTabScreen({super.key});

  @override
  State<HomeTabScreen> createState() => _HomeTabScreenState();
}

class _HomeTabScreenState extends State<HomeTabScreen> {
  final PageController _pageController = PageController(
    initialPage: 10000,
  ); // Start at a high number for infinite scroll
  int _currentPage = 0;
  Timer? _timer;

  final List<BannerObject> _carouselBanners = [
    BannerObject(
      imageUrl:
          'https://gacha-scheduler.s3.ap-northeast-2.amazonaws.com/banner/HSR/3_6/1.webp',
      description: '「에버나이트」 (얼음/기억)',
      startDate: DateTime(2025, 9, 24),
      endDate: DateTime(2025, 10, 15),
      textColor: Colors.white,
    ),
    BannerObject(
      imageUrl:
          'https://gacha-scheduler.s3.ap-northeast-2.amazonaws.com/banner/HSR/3_6/2.webp',
      description: '「단항·등황」 (물리/보존)',
      startDate: DateTime(2025, 10, 15),
      endDate: DateTime(2025, 11, 4),
      textColor: Colors.yellowAccent,
    ),
  ];

  @override
  void initState() {
    super.initState();
    _startAutoScroll();
  }

  @override
  void dispose() {
    _stopAutoScroll();
    _pageController.dispose();
    super.dispose();
  }

  void _startAutoScroll() {
    _timer = Timer.periodic(const Duration(seconds: 4), (timer) {
      if (_pageController.hasClients) {
        int nextPage = _pageController.page!.toInt() + 1;
        _pageController.animateToPage(
          nextPage,
          duration: const Duration(milliseconds: 400),
          curve: Curves.easeIn,
        );
      }
    });
  }

  void _stopAutoScroll() {
    _timer?.cancel();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(LocalizationManager.instance.getString('home_label')),
        centerTitle: true,
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(
              horizontal: 16.0,
              vertical: 8.0,
            ), // Add horizontal padding
            child: ClipRRect(
              borderRadius: BorderRadius.circular(12.0), // Add rounded corners
              child: SizedBox(
                height: 200.0, // Carousel height
                child: PageView.builder(
                  controller: _pageController,
                  itemCount:
                      _carouselBanners.length *
                      10000, // Simulate infinite scroll
                  onPageChanged: (int page) {
                    setState(() {
                      _currentPage = page % _carouselBanners.length;
                    });
                  },
                  itemBuilder: (BuildContext context, int index) {
                    final banner =
                        _carouselBanners[index % _carouselBanners.length];
                    return Stack(
                      children: [
                        CachedNetworkImage(
                          imageUrl: banner.imageUrl,
                          fit: BoxFit.cover,
                          width: double.infinity,
                          placeholder: (context, url) =>
                              const Center(child: CircularProgressIndicator()),
                          errorWidget: (context, url, error) =>
                              const Icon(Icons.error),
                        ),
                        Align(
                          alignment: Alignment.bottomCenter,
                          child: Container(
                            height: 60.0, // 30% of 200.0
                            decoration: BoxDecoration(
                              gradient: LinearGradient(
                                begin: Alignment.topCenter,
                                end: Alignment.bottomCenter,
                                colors: [
                                  Colors.black.withOpacity(
                                    0.0,
                                  ), // Start with more transparency
                                  Colors.black.withOpacity(0.7),
                                ],
                              ),
                            ),
                          ),
                        ),
                        Positioned(
                          bottom:
                              10.0, // Adjusted position to make space for dates
                          left: 20.0,
                          right: 20.0,
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                banner.description,
                                style: TextStyle(
                                  color: banner.textColor,
                                  fontSize: 18.0,
                                  fontWeight: FontWeight.bold,
                                ),
                                maxLines: 2,
                                overflow: TextOverflow.ellipsis,
                              ),
                              const SizedBox(height: 4.0),
                              Text(
                                '${banner.startDate.toIso8601String().substring(0, 10)} ~ ${banner.endDate.toIso8601String().substring(0, 10)}',
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 12.0,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    );
                  },
                ),
              ),
            ),
          ),
          Expanded(
            child: Center(
              child: Text(LocalizationManager.instance.getString('app_title')),
            ),
          ),
        ],
      ),
    );
  }
}
