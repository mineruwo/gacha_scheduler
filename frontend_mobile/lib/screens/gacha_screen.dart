import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';
import 'package:frontend_mobile/models/gacha_banner.dart';
import 'package:frontend_mobile/models/gacha_character.dart';
import 'package:frontend_mobile/providers/gacha_providers.dart';
import 'package:frontend_mobile/theme/app_theme.dart';

class GachaScreen extends ConsumerWidget {
  const GachaScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bannersAsync = ref.watch(bannersProvider);
    final l10n = LocalizationManager.instance;

    ref.listen<GachaState>(gachaControllerProvider, (previous, next) {
      if (next.error != null && previous?.error != next.error) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(l10n.getString('gacha_pull_error'))),
        );
      }
    });

    return bannersAsync.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (error, stackTrace) => _LoadError(onRetry: () => ref.invalidate(bannersProvider)),
      data: (banners) {
        if (banners.isEmpty) {
          return Center(child: Text(l10n.getString('gacha_no_banners')));
        }
        return _GachaBody(banners: banners);
      },
    );
  }
}

class _LoadError extends StatelessWidget {
  final VoidCallback onRetry;

  const _LoadError({required this.onRetry});

  @override
  Widget build(BuildContext context) {
    final l10n = LocalizationManager.instance;
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(l10n.getString('gacha_load_error')),
          const SizedBox(height: 12),
          FilledButton(
            onPressed: onRetry,
            child: Text(l10n.getString('gacha_retry')),
          ),
        ],
      ),
    );
  }
}

class _GachaBody extends ConsumerWidget {
  final List<GachaBanner> banners;

  const _GachaBody({required this.banners});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(gachaControllerProvider);
    final controller = ref.read(gachaControllerProvider.notifier);
    final l10n = LocalizationManager.instance;

    final selected = state.selectedBanner;
    if (selected == null) {
      // 최초 진입 시 첫 배너 자동 선택
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (context.mounted) controller.selectBanner(banners.first);
      });
      return const Center(child: CircularProgressIndicator());
    }

    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            DropdownButtonFormField<GachaBanner>(
              initialValue: selected,
              isExpanded: true,
              decoration: InputDecoration(
                labelText: l10n.getString('gacha_select_banner'),
              ),
              items: banners
                  .map((b) => DropdownMenuItem(
                        value: b,
                        child: Text('[${b.gameName}] ${b.name}', overflow: TextOverflow.ellipsis),
                      ))
                  .toList(),
              onChanged: state.isPulling
                  ? null
                  : (banner) {
                      if (banner != null) controller.selectBanner(banner);
                    },
            ),
            const SizedBox(height: 12),
            _PityCounter(pityCount: state.pityCount, threshold: selected.pityThreshold),
            const SizedBox(height: 12),
            Expanded(
              child: state.lastResults.isEmpty
                  ? Center(
                      child: Text(
                        l10n.getString('gacha_no_results'),
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Colors.grey),
                      ),
                    )
                  : _ResultGrid(results: state.lastResults),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: state.isPulling ? null : () => controller.pull(1),
                    child: Text(l10n.getString('gacha_pull_one')),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: FilledButton(
                    onPressed: state.isPulling ? null : () => controller.pull(10),
                    child: state.isPulling
                        ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : Text(l10n.getString('gacha_pull_ten')),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _PityCounter extends StatelessWidget {
  final int pityCount;
  final int threshold;

  const _PityCounter({required this.pityCount, required this.threshold});

  @override
  Widget build(BuildContext context) {
    final l10n = LocalizationManager.instance;
    final progress = threshold > 0 ? (pityCount / threshold).clamp(0.0, 1.0) : 0.0;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: AppTheme.softCard(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '${l10n.getString('gacha_pity_label')}: $pityCount / $threshold',
            style: const TextStyle(fontWeight: FontWeight.bold, color: AppColors.textPrimary),
          ),
          const SizedBox(height: 8),
          ClipRRect(
            borderRadius: BorderRadius.circular(6),
            child: LinearProgressIndicator(value: progress, minHeight: 10),
          ),
        ],
      ),
    );
  }
}

class _ResultGrid extends StatelessWidget {
  final List<GachaCharacter> results;

  const _ResultGrid({required this.results});

  static Color _rarityColor(int rarity) {
    switch (rarity) {
      case >= 5:
        return AppColors.rarity5;
      case 4:
        return AppColors.rarity4;
      case 3:
        return AppColors.rarity3;
      default:
        return AppColors.rarityDefault;
    }
  }

  @override
  Widget build(BuildContext context) {
    return GridView.builder(
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 5,
        mainAxisSpacing: 8,
        crossAxisSpacing: 8,
        childAspectRatio: 0.72,
      ),
      itemCount: results.length,
      itemBuilder: (context, index) {
        final character = results[index];
        final color = _rarityColor(character.rarity);
        return Container(
          decoration: BoxDecoration(
            border: Border.all(color: color, width: 2),
            borderRadius: BorderRadius.circular(14),
            color: color.withValues(alpha: 0.18),
          ),
          padding: const EdgeInsets.all(4),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.person_rounded, color: AppColors.textPrimary, size: 28),
              const SizedBox(height: 4),
              Text(
                character.name,
                textAlign: TextAlign.center,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(fontSize: 10, color: AppColors.textPrimary),
              ),
              const SizedBox(height: 2),
              Text(
                '★${character.rarity}',
                style: TextStyle(fontSize: 10, color: AppColors.textPrimary, fontWeight: FontWeight.bold),
              ),
            ],
          ),
        );
      },
    );
  }
}
