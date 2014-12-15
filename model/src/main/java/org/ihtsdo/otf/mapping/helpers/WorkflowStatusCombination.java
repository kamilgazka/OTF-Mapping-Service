package org.ihtsdo.otf.mapping.helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class WorkflowStatusCombination.
 *
 * @author ${author}
 */
public class WorkflowStatusCombination {

  /**
   * The workflow statuses and count for each one E.g. CONFLICT_DETECTED,
   * CONFLICTED_DETECTED -> (CONFLICT_DETECTED, 2) .
   */
  Map<WorkflowStatus, Integer> workflowStatuses = new HashMap<>();

  /**
   * Instantiates an empty {@link WorkflowStatusCombination}.
   */
  public WorkflowStatusCombination() {

  }
  
  /**
   * Instantiates a {@link WorkflowStatusCombination} from the specified parameters.
   *
   * @param workflowStatuses the workflow statuses
   */
  public WorkflowStatusCombination(List<WorkflowStatus> workflowStatuses) {
    for (WorkflowStatus w : workflowStatuses) {
      addWorkflowStatus(w);
    }
  }

 

  /**
   * Adds the workflow status.
   *
   * @param workflowStatus the workflow status
   */
  public void addWorkflowStatus(WorkflowStatus workflowStatus) {

    // check for null map
    if (workflowStatuses == null)
      workflowStatuses = new HashMap<>();

    // if this contains this workflow status, increment count
    workflowStatuses.put(
        workflowStatus,
        workflowStatuses.get(workflowStatus) == null ? 1 : workflowStatuses
            .get(workflowStatus) + 1);

  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((workflowStatuses == null) ? 0 : workflowStatuses.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    WorkflowStatusCombination other = (WorkflowStatusCombination) obj;
    if (workflowStatuses == null) {
      if (other.workflowStatuses != null)
        return false;
    } else if (!workflowStatuses.equals(other.workflowStatuses))
      return false;
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "WorkflowStatusCombination [workflowStatuses=" + workflowStatuses
        + "]";
  }
  
  
}
