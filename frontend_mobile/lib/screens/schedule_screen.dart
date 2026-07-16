import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:frontend_mobile/managers/localization_manager.dart';
import 'package:frontend_mobile/models/game_model.dart';
import 'package:frontend_mobile/models/schedule_event_model.dart';
import 'package:frontend_mobile/providers/schedule_providers.dart';
import 'package:frontend_mobile/theme/app_theme.dart';

const Map<String, String> _categoryLabels = {
  'UPDATE': '업데이트',
  'EVENT': '이벤트',
  'MAINTENANCE': '점검',
};

Color _categoryColor(String category) {
  switch (category) {
    case 'UPDATE':
      return AppColors.categoryUpdate;
    case 'EVENT':
      return AppColors.categoryEvent;
    case 'MAINTENANCE':
      return AppColors.categoryMaintenance;
    default:
      return AppColors.rarityDefault;
  }
}

class ScheduleScreen extends ConsumerWidget {
  const ScheduleScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = LocalizationManager.instance;
    final range = ref.watch(scheduleRangeProvider);
    final rangeController = ref.read(scheduleRangeProvider.notifier);
    final gamesAsync = ref.watch(gamesProvider);
    final schedulesAsync = ref.watch(schedulesProvider);
    final selectedCodes = ref.watch(gameFilterProvider);
    final filterController = ref.read(gameFilterProvider.notifier);

    return Scaffold(
      appBar: AppBar(title: Text(l10n.getString('schedule_screen_title'))),
      body: Column(
        children: [
          _RangeToolbar(range: range, onShift: rangeController.shift, onReset: rangeController.reset),
          gamesAsync.when(
            data: (games) => games.isEmpty
                ? const SizedBox.shrink()
                : _GameFilterRow(
                    games: games,
                    selectedCodes: selectedCodes,
                    onToggle: filterController.toggle,
                  ),
            loading: () => const SizedBox.shrink(),
            error: (_, _) => const SizedBox.shrink(),
          ),
          const _Legend(),
          const SizedBox(height: 4),
          Expanded(
            child: schedulesAsync.when(
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (_, _) => Center(child: Text(l10n.getString('schedule_load_error'))),
              data: (schedules) {
                final visible = selectedCodes.isEmpty
                    ? schedules
                    : schedules.where((s) => selectedCodes.contains(s.gameCode)).toList();
                visible.sort((a, b) {
                  final byTitle = (a.gameTitle ?? '').compareTo(b.gameTitle ?? '');
                  return byTitle != 0 ? byTitle : a.startAt.compareTo(b.startAt);
                });
                if (visible.isEmpty) {
                  return Center(
                    child: Text(
                      l10n.getString('schedule_no_events'),
                      style: const TextStyle(color: AppColors.textSecondary),
                    ),
                  );
                }
                return _GanttChart(range: range, events: visible);
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _RangeToolbar extends StatelessWidget {
  final DateTimeRange range;
  final void Function(int months) onShift;
  final VoidCallback onReset;

  const _RangeToolbar({required this.range, required this.onShift, required this.onReset});

  String _fmt(DateTime d) => '${d.year}.${d.month.toString().padLeft(2, '0')}.${d.day.toString().padLeft(2, '0')}';

  @override
  Widget build(BuildContext context) {
    final l10n = LocalizationManager.instance;
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 4),
        decoration: AppTheme.softCard(),
        child: Row(
          children: [
            IconButton(
              onPressed: () => onShift(-1),
              icon: const Icon(Icons.chevron_left_rounded, color: AppColors.primaryDark),
            ),
            Expanded(
              child: Text(
                '${_fmt(range.start)} ~ ${_fmt(range.end)}',
                textAlign: TextAlign.center,
                style: const TextStyle(fontWeight: FontWeight.bold, color: AppColors.textPrimary, fontSize: 13),
              ),
            ),
            IconButton(
              onPressed: () => onShift(1),
              icon: const Icon(Icons.chevron_right_rounded, color: AppColors.primaryDark),
            ),
            TextButton(onPressed: onReset, child: Text(l10n.getString('schedule_today_button'))),
          ],
        ),
      ),
    );
  }
}

class _GameFilterRow extends StatelessWidget {
  final List<GameModel> games;
  final List<String> selectedCodes;
  final void Function(String gameCode) onToggle;

  const _GameFilterRow({required this.games, required this.selectedCodes, required this.onToggle});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 44,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: games.length,
        separatorBuilder: (_, _) => const SizedBox(width: 8),
        itemBuilder: (context, index) {
          final game = games[index];
          final selected = selectedCodes.contains(game.gameCode);
          return FilterChip(
            label: Text(game.title),
            selected: selected,
            onSelected: (_) => onToggle(game.gameCode),
            selectedColor: AppColors.primary,
            checkmarkColor: Colors.white,
            backgroundColor: Colors.white,
            labelStyle: TextStyle(
              color: selected ? Colors.white : AppColors.textPrimary,
              fontWeight: FontWeight.w600,
              fontSize: 12,
            ),
            side: BorderSide(color: selected ? AppColors.primary : const Color(0xFFE6E1F5)),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          );
        },
      ),
    );
  }
}

class _Legend extends StatelessWidget {
  const _Legend();

  Widget _dot(Color color, String label) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(width: 8, height: 8, decoration: BoxDecoration(color: color, shape: BoxShape.circle)),
        const SizedBox(width: 4),
        Text(label, style: const TextStyle(fontSize: 12, color: AppColors.textSecondary)),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
      child: Row(
        children: [
          _dot(AppColors.categoryUpdate, _categoryLabels['UPDATE']!),
          const SizedBox(width: 14),
          _dot(AppColors.categoryEvent, _categoryLabels['EVENT']!),
          const SizedBox(width: 14),
          _dot(AppColors.categoryMaintenance, _categoryLabels['MAINTENANCE']!),
        ],
      ),
    );
  }
}

class _GanttChart extends StatelessWidget {
  final DateTimeRange range;
  final List<ScheduleEventModel> events;

  const _GanttChart({required this.range, required this.events});

  List<DateTime> _monthTicks() {
    final ticks = <DateTime>[];
    var cursor = DateTime(range.start.year, range.start.month, 1);
    while (!cursor.isAfter(range.end)) {
      if (!cursor.isBefore(range.start)) ticks.add(cursor);
      cursor = DateTime(cursor.year, cursor.month + 1, 1);
    }
    return ticks;
  }

  double _position(DateTime date) {
    final fromMs = range.start.millisecondsSinceEpoch;
    final toMs = range.end.millisecondsSinceEpoch;
    final value = (date.millisecondsSinceEpoch - fromMs) / (toMs - fromMs);
    return value.clamp(0.0, 1.0);
  }

  @override
  Widget build(BuildContext context) {
    final ticks = _monthTicks();
    final now = DateTime.now();
    final todayVisible = !now.isBefore(range.start) && !now.isAfter(range.end);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: LayoutBuilder(
        builder: (context, constraints) {
          final trackWidth = constraints.maxWidth;
          return Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              SizedBox(
                height: 18,
                child: Stack(
                  children: ticks.map((tick) {
                    return Positioned(
                      left: _position(tick) * trackWidth,
                      child: Text(
                        '${tick.year}.${tick.month.toString().padLeft(2, '0')}',
                        style: const TextStyle(fontSize: 10, color: AppColors.textSecondary),
                      ),
                    );
                  }).toList(),
                ),
              ),
              const Divider(height: 12, color: Color(0xFFE6E1F5)),
              Expanded(
                child: ListView.separated(
                  itemCount: events.length,
                  separatorBuilder: (_, _) => const SizedBox(height: 14),
                  itemBuilder: (context, index) {
                    final event = events[index];
                    final start = _position(event.startAt);
                    final end = event.endAt != null ? _position(event.endAt!) : 1.0;
                    final color = _categoryColor(event.category);
                    final barLeft = start * trackWidth;
                    final barWidth = ((end - start) * trackWidth).clamp(4.0, trackWidth - barLeft);

                    return Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          event.title,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: AppColors.textPrimary),
                        ),
                        Text(
                          '${event.gameTitle ?? event.gameCode} · ${_categoryLabels[event.category] ?? event.category}',
                          style: const TextStyle(fontSize: 11, color: AppColors.textSecondary),
                        ),
                        const SizedBox(height: 6),
                        SizedBox(
                          height: 10,
                          child: Stack(
                            children: [
                              Container(
                                height: 4,
                                margin: const EdgeInsets.only(top: 3),
                                decoration: BoxDecoration(
                                  color: const Color(0xFFEDEAF7),
                                  borderRadius: BorderRadius.circular(2),
                                ),
                              ),
                              if (todayVisible)
                                Positioned(
                                  left: _position(now) * trackWidth,
                                  child: Container(width: 2, height: 10, color: AppColors.primaryDark),
                                ),
                              Positioned(
                                left: barLeft,
                                width: barWidth,
                                child: Container(
                                  height: 10,
                                  decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(5)),
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
            ],
          );
        },
      ),
    );
  }
}
