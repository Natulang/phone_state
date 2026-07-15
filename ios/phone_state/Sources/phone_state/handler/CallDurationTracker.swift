import Foundation

final class CallDurationTracker {
    private let now: () -> Date
    private var startTime: Date?
    private(set) var duration: Int = 0

    init(now: @escaping () -> Date = Date.init) {
        self.now = now
    }

    var isRunning: Bool {
        startTime != nil
    }

    func start() {
        startTime = now()
        duration = 0
    }

    func startIfNeeded() {
        if startTime == nil {
            start()
        }
    }

    @discardableResult
    func updateDuration() -> Int {
        guard let startTime else {
            return duration
        }

        duration = Int(now().timeIntervalSince(startTime))
        return duration
    }

    func stop() {
        startTime = nil
    }

    func reset() {
        stop()
        duration = 0
    }
}
