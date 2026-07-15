//
//  PhoneStateHandler.swift
//  phone_state
//
//  Created by Andrea Mainella on 28/02/22.
//

import Foundation
import CallKit
import Flutter

@available(iOS 13.0, *)
class PhoneStateHandler: NSObject, FlutterStreamHandler, CXCallObserverDelegate {
    
    private var _eventSink: FlutterEventSink?
    private var callObserver = CXCallObserver()
    private let callDurationTracker = CallDurationTracker()
    private var durationTimer: Timer?
    
    override init() {
        super.init()
        callObserver.setDelegate(self, queue: nil)
    }
    
    private func getCallState(from call: CXCall) -> PhoneStateStatus {
        if !call.isOutgoing && !call.hasConnected && !call.hasEnded {
            return .CALL_INCOMING
        } else if call.isOutgoing && !call.hasConnected && !call.hasEnded {
            return .CALL_OUTGOING
        } else if (call.hasConnected && !call.hasEnded && !call.isOnHold) {
            return .CALL_STARTED
        } else if call.hasEnded {
            return .CALL_ENDED
        } else {
            return .NOTHING
        }
    }
    
    private func startDurationTimer() {
        callDurationTracker.start()
        durationTimer?.invalidate()
        durationTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) {
            [weak self]_ in guard let self = self else { return }
            if self.callDurationTracker.isRunning {
                self.callDurationTracker.updateDuration()
                self.sendCallState(.CALL_STARTED)
            }
        }
    }
    
    private func startDurationTimerIfNeeded() {
        if !callDurationTracker.isRunning {
            startDurationTimer()
        }
    }

    private func stopDurationTimer() {
        durationTimer?.invalidate()
        durationTimer = nil
        callDurationTracker.stop()
    }
    
    private func resetCallDuration() {
        stopDurationTimer()
        callDurationTracker.reset()
    }
    
    private func sendCallState(_ status: PhoneStateStatus) {
        if let eventSink = _eventSink {
            eventSink(
                [
                    "status": status.rawValue,
                    /*
                     Cannot get phone number on iOS
                     */
                    "phoneNumber": nil,
                    "callDuration": callDurationTracker.duration
                ]
            )
        }
    }
    
    public func callObserver(_ callObserver: CXCallObserver, callChanged call: CXCall) {
        let status = getCallState(from: call)
        
        switch status {
            /*
             Incoming call ringing — reset duration timer
             */
        case.CALL_INCOMING:
            resetCallDuration()
            /*
             Outgoing dialing — start duration as early as CallKit reports the call
             */
        case.CALL_OUTGOING:
            startDurationTimerIfNeeded()
            /*
             Call connected (incoming answered or outgoing connected)
             */
        case.CALL_STARTED:
            startDurationTimerIfNeeded()
            /*
             Call finished
             */
        case.CALL_ENDED:
            stopDurationTimer()
        default:
            break
        }
        
        sendCallState(status)
    }
    
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        _eventSink = events
        var initialStatus = PhoneStateStatus.NOTHING
        for call in callObserver.calls {
            let callStatus = getCallState(from: call)
            if callStatus != .NOTHING {
                initialStatus = callStatus
                
                switch callStatus {
                    /*
                     Reset duration timer for new incoming calls
                     */
                case.CALL_INCOMING:
                    resetCallDuration()
                    /*
                     Start duration as early as CallKit reports outgoing calls
                     */
                case.CALL_OUTGOING:
                    startDurationTimerIfNeeded()
                    /*
                     Start timer if a call is already in progress
                     */
                case.CALL_STARTED:
                    startDurationTimerIfNeeded()
                default:
                    break
                }
                break
            }
        }
        
        sendCallState(initialStatus)
        
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        stopDurationTimer()
        _eventSink = nil
        return nil
    }
    
    deinit {
        stopDurationTimer()
    }
}
