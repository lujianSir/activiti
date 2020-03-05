package excute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import com.yangqi.pojo.Employee;

/**
* @author YangQi
* @version 创建时间：<br>
*				   2018年8月14日 下午9:04:26
* @ClassName 类名称<br>
* @Description 类描述<br>
*/
public class LeaveProcessExa {

	//获取默认的流程引擎
	private ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	
	//实例流程id,用来记录流程,以便获取当前任务
	private String processInstanceId;
	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	//员工类,用来存储信息
	private Employee employee;
	//部署流程
	@Test
	public void deploymentProcess(){
		Deployment deployment = processEngine.getRepositoryService()
				.createDeployment()
				.name("肯定是最终版004---")
				.addClasspathResource("diagrams/LeaveProcess.bpmn")
				.addClasspathResource("diagrams/LeaveProcess.png")
				.deploy();
		System.out.println("部署工程id:"+deployment.getId());
		System.out.println("部署工程name:"+deployment.getName());
	}
	
	//初始化请假员工信息
	public void setDeployee(int day,String name){
		employee = new Employee();
		employee.setDay(day);
		employee.setEmployeeName(name);
	}
	
	//开启流程   开始请假
	@Test
	public void startLeaveProcess(){
		
		//获取runtimeservice对象
		RuntimeService runtimeService = this.processEngine.getRuntimeService();
		
		
		//根据流程key值,获取流程
		String processKey = "leaveProcess";
		
		//将信息加入map,以便传入流程中
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("employeeName", "李四");
		variables.put("day",10);
		variables.put("reason","回家看亲戚");
		variables.put("leadName", "李四");	
		
		//开启流程
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, variables);
		//将得到的实例流程id值赋给之前设置的变量
		processInstanceId = processInstance.getId();
		System.out.println("流程开启成功.......实例流程id:"+processInstanceId);
	}
	
	@Test
	public void put(){
		System.out.println(processInstanceId);
	}
	
	//开始走流程
	//1.获取当前任务,并且完成   根据实例ID进行操作
	@Test
	public void getTaskAndComplete(){
		processInstanceId = "12501";
		//获取taskservice实例
		TaskService taskService = this.processEngine.getTaskService();
		
		//查询流程实例，如果走完了 ，应该是null,否则应该有当前任务
		Object o=this.processEngine.getRuntimeService()
		.createProcessInstanceQuery()//获取查询对象
		.processInstanceId(processInstanceId)//根据id查询流程实例
		.singleResult();//获取查询结果,如果为空,说明这个流程已经执行完毕,否则,获取任务并执行
		if(o!=null) {
			//通过流程实例ID，查询当前的任务
			Task task = taskService.createTaskQuery()//创建查询对象
					.processInstanceId(processInstanceId)//通过流程实例id来查询当前任务
					.singleResult();//获取单个查询结果
			String taskName = task.getName();				
			
			if(taskName.equals("发起申请")){//职员节点
				completeEmployeeTask(task);
			}else if(taskName.equals("领导审批")){//领导节点
				completeLeaderTask(task);
			}else{//经理节点
				completeJingliTask(task);
			}
		}
		
		//开始进行流程
//		while(this.processEngine.getRuntimeService()
//				.createProcessInstanceQuery()//获取查询对象
//				.processInstanceId(processInstanceId)//根据id查询流程实例
//				.singleResult()//获取查询结果,如果为空,说明这个流程已经执行完毕,否则,获取任务并执行
//				!=null){
//			Task task = taskService.createTaskQuery()//创建查询对象
//				.processInstanceId(processInstanceId)//通过流程实例id来查询当前任务
//				.singleResult();//获取单个查询结果
//			String taskName = task.getName();
//			if(taskName.equals("发起申请")){//职员节点
//				completeEmployeeTask(task);
//			}else if(taskName.equals("领导审批")){//领导节点
//				completeLeaderTask(task);
//			}else{//经理节点
//				completeJingliTask(task);
//			}
//		}
		
		System.out.println("审核结束..........");
	}
	
	
	@Test //通过名称查询当前用户所以的任务
	public void getTaskList() {
		TaskService taskService = this.processEngine.getTaskService();
		//3.根据流程定义的key,负责人assignee来实现当前用户的任务列表查询
        List<Task> taskList = taskService.createTaskQuery()
                .processDefinitionKey("leaveProcess")
                .taskAssignee("李四")
                .list();
        //4.任务列表的展示
        for(Task task :taskList){
            System.out.println("流程实例ID:"+task.getProcessInstanceId());
            System.out.println("任务ID:"+task.getId());
            System.out.println("任务负责人:"+task.getAssignee());
            System.out.println("任务名称:"+task.getName());  
        }

	}
	
	
	@Test //查询当前正在执行任务 所有的参数 （任务或者实例都可以查询出来）
	public void selectTask() {
		processInstanceId = "20001";
		RuntimeService runtimeService=processEngine.getRuntimeService();
		Map<String, Object> map=runtimeService.getVariables(processInstanceId);
		System.out.println("传递的参数："+map);
	}
	
	
	@Test//删除流程实例  (取消申请)
	public void deletProcessInstance() {
		processInstanceId = "12501";
		RuntimeService runtimeService=processEngine.getRuntimeService();
		runtimeService.deleteProcessInstance(processInstanceId,"删除原因");//删除流程
		System.out.println("成功");
	}
	
	
	
	/**查询历史流程变量实例*/
	@Test
	public void findHistoryProcessVariables(){
		String processInstanceId = "20001";
		List<HistoricVariableInstance> list = processEngine.getHistoryService()//
						.createHistoricVariableInstanceQuery()//创建一个历史的流程变量查询对象
						.processInstanceId(processInstanceId)//
						.list();
		if(list!=null && list.size()>0){
			for(HistoricVariableInstance hvi:list){
				System.out.println(hvi.getId()+"   "+
								hvi.getProcessInstanceId()+"   "+
								hvi.getVariableName()+"   "+
								hvi.getVariableTypeName()+"    "+
								hvi.getValue());
				System.out.println("###############################################");
			}
		}
	}

	
	
	//职员提交申请
	public void completeEmployeeTask(Task task){
		//获取任务id
		String taskId = task.getId();
		Map<String, Object> variables = new HashMap<String, Object>();		
		variables.put("leaveStatus", 1);
		//完成任务
		this.processEngine.getTaskService().complete(taskId);
		System.out.println("职员已经提交申请.......");
		
	}
	//领导审批
	public void completeLeaderTask(Task task){
		//获取任务id
		String taskId = task.getId();
				
		//领导意见
		Map<String, Object> variables = new HashMap<String, Object>();
		//variables.put("day",4);
		variables.put("leaderResult", 0);
		variables.put("leaveStatus", 0);//打回
		//完成任务
		this.processEngine.getTaskService().complete(taskId, variables);
		System.out.println("领导审核完毕........");
		
	}
	//经理审批
	public void completeJingliTask(Task task){
		//获取任务id
		String taskId = task.getId();
		String name = task.getName();
		//经理意见
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("result", 1);
		//完成任务
		this.processEngine.getTaskService().complete(taskId, variables);
		System.out.println("经理审核完毕........,审核经理:"+name);
		
	}
}
