package org.sopt.sopkathon.domain.callrequest.entity;

public enum CallRequestStatus {
  /** 요청을 보냈고 아직 통화 확인 전 */
  PENDING,
  /** "통화하셨나요?"에서 예를 누름 */
  CONNECTED
}
