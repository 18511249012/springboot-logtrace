package com.hzcard.logtrace.event;

public class EventIimmutable {

	private String eventType;
	
	private String eventId;
	
	private String eventCode;
	
	private String eventPlatform;
	
	private String eventSequence;
	
	
	public EventIimmutable(String eventPlatform,String eventType,String eventId,String eventCode,String eventSequence){
		this.eventType = eventType;
		this.eventId = eventId;
		this.eventCode = eventCode;
		this.eventPlatform = eventPlatform;
		this.eventSequence=eventSequence;
	}

	public String getEventType() {
		return eventType;
	}

	public String getEventId() {
		return eventId;
	}

	public String getEventCode() {
		return eventCode;
	}

	public String getEventPlatform() {
		return eventPlatform;
	}


	public String getEventSequence() {
		return eventSequence;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventCode == null) ? 0 : eventCode.hashCode());
		result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
		result = prime * result + ((eventPlatform == null) ? 0 : eventPlatform.hashCode());
		result = prime * result + ((eventSequence == null) ? 0 : eventSequence.hashCode());
		result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventIimmutable other = (EventIimmutable) obj;
		if (eventCode == null) {
			if (other.eventCode != null)
				return false;
		} else if (!eventCode.equals(other.eventCode))
			return false;
		if (eventId == null) {
			if (other.eventId != null)
				return false;
		} else if (!eventId.equals(other.eventId))
			return false;
		if (eventPlatform == null) {
			if (other.eventPlatform != null)
				return false;
		} else if (!eventPlatform.equals(other.eventPlatform))
			return false;
		if (eventSequence == null) {
			if (other.eventSequence != null)
				return false;
		} else if (!eventSequence.equals(other.eventSequence))
			return false;
		if (eventType == null) {
			if (other.eventType != null)
				return false;
		} else if (!eventType.equals(other.eventType))
			return false;
		return true;
	}

	

	
	
	
}
