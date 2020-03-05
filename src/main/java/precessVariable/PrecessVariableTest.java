package precessVariable;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;

public class PrecessVariableTest {

	//获取默认的流程引擎
	private ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	
	//部署流程
		@Test
		public void deploymentProcess(){
			Deployment deployment = processEngine.getRepositoryService()//与流程定义相关的service
					.createDeployment()
					.name("请假流程01")
					.addClasspathResource("diagrams/precessVariable.bpmn")
					.addClasspathResource("diagrams/precessVariable.png")
					.deploy();//完成部署
			System.out.println("部署工程id:"+deployment.getId());
			System.out.println("部署工程name:"+deployment.getName());
		}
		
		//启动流程实例
		@Test
		public void startProcessInstance() {
			//流程定义的key
			String processDefinitionKey="precessVariable";
			//获取runtimeservice对象
			RuntimeService runtimeServic=processEngine.getRuntimeService();//与正在执行的流程实例和执行对象相关的service
			ProcessInstance  pi=runtimeServic.startProcessInstanceByKey(processDefinitionKey);//使用流程定义的key启动流程
			System.out.println("流程实例ID"+pi.getId());//流程实例的id
			System.out.println("流程定义ID"+pi.getProcessDefinitionId());//流程定义ID
		}
		
		//设置流程变量
		public void setVariables() {
			
		}
		
		
		
		//获取流程变量
		public void getVariables() {
			
		}
		
		public void setAndGetVariables() {
			//流程实例，执行对象
			RuntimeService runtimeService=processEngine.getRuntimeService();
			//与任务（正在执行）
			//runtimeService.setVariable(executionId, variableName, value);
			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("employeeName", "廉斌");
			variables.put("day",10);
			runtimeService.setVariables("2501", variables);
		}
}
