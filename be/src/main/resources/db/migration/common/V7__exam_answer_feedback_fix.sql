alter table public.exam_sentence_feedback
    alter column target_sentence type TEXT using target_sentence::TEXT;

alter table public.exam_sentence_feedback
    alter column target_segment type TEXT using target_segment::TEXT;

alter table public.exam_sentence_feedback
    alter column corrected_segment type TEXT using corrected_segment::TEXT;

alter table public.exam_sentence_feedback
    alter column comment type TEXT using comment::TEXT;

